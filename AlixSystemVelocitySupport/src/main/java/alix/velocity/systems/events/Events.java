package alix.velocity.systems.events;


import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.VerifiedCache;
import alix.common.login.LoginVerdict;
import alix.common.login.premium.*;
import alix.common.reflection.CommonReflection;
import alix.common.utils.AlixCache;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.floodgate.GeyserUtil;
import alix.velocity.server.AlixVelocityLimbo;
import alix.velocity.systems.events.premium.EncryptionInfo;
import alix.velocity.systems.packets.PacketEventListener;
import alix.velocity.utils.AlixChannelInitInterceptor;
import alix.velocity.utils.user.UserManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.network.ListenerType;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import io.netty.channel.Channel;
import ua.nanit.limbo.connection.login.LoginInfo;
import ua.nanit.limbo.protocol.registry.Version;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static alix.common.login.premium.PremiumVerifier.keyPair;

public final class Events {

    //private static final JavaNetInetAddressAccess access = SharedSecrets.getJavaNetInetAddressAccess();
    private final boolean authorizeLinked = true, authorizePremium = true;
    private final GeyserUtil util;
    //private final VelocityConnectionFilter[] filters = AlixChannelInitInterceptor.getConnectionFilters();
    //private final LimboRegisteredServer limboRegisteredServer;

    public Events(GeyserUtil util) {
        this.util = util;
        //this.limboRegisteredServer = new LimboRegisteredServer(server);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onListenerBound(ListenerBoundEvent event) {
        if (event.getListenerType() != ListenerType.MINECRAFT)
            return;

        AlixChannelInitInterceptor.initEndpoints();
    }

    /*@Subscribe
    public void onPing(ProxyPingEvent event) {
        Main.logInfo("ProxyPingEvent=" + event);
    }*/

    @Subscribe(order = PostOrder.FIRST)
    public void onInitialServer(PlayerChooseInitialServerEvent event, Continuation continuation) {
        //Main.logInfo("onInitialServer=" + event.getPlayer());
        ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        var data = UserFileManager.get(player.getUsername());
        var channel = player.getConnection().getChannel();

        LimboJoinProfiler.update(channel, ConnectionStage.SERVER_CHOOSE_INITIAL_SERVER);
        //Main.logInfo("onInitialServer");

        boolean bedrockPremium = this.authorizeLinked && this.util.isLinked(channel);

        //*should* be safe
        boolean premium = bedrockPremium || this.authorizePremium /*&& data != null && data.getPremiumData().getStatus().isPremium()*/
                                            && VerifiedCache.getAndCheckIfEquals(player.getUsername(), channel);
        boolean requirePremiumPass = ConfigParams.requireRegisterFromAll && data == null;
        if (premium && !requirePremiumPass) {
            LoginInfo.set(channel, data != null, data != null ? LoginVerdict.LOGIN_PREMIUM : LoginVerdict.REGISTER_PREMIUM);
            UserManager.add(player);
            continuation.resume();
            return;
        }

        if (data != null && data.getLoginParams().getIpAutoLogin() && data.getSavedIP().equals(player.getRemoteAddress().getAddress())) {
            LoginInfo.set(channel, true, LoginVerdict.IP_AUTO_LOGIN);
            UserManager.add(player);
            continuation.resume();
            return;
        }

        //Main.logInfo("onInitialServer2=" + event.getPlayer());
        AlixVelocityLimbo.initAfterLoginSuccess(player, continuation, this.util);
    }

    public static final Map<Channel, EncryptionInfo> preLoginContinuations = AlixCache.newBuilder().weakKeys().maximumSize(300).<Channel, EncryptionInfo>build().asMap();
    private static final Field delegate = CommonReflection.getDeclaredFieldAccessible(LoginInboundConnection.class, "delegate");

    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(PreLoginEvent event, Continuation continuation) throws IllegalAccessException {
        //Main.logInfo("onPreLogin=" + event.getResult() + " " + event.getUsername() + " " + event.getUniqueId());
        if (!LimboJoinProfiler.PROFILE_JOINS && !event.getResult().isAllowed()) return;

        InetAddress ip = event.getConnection().getRemoteAddress().getAddress();

        LoginInboundConnection connection = (LoginInboundConnection) event.getConnection();
        InitialInboundConnection initial = (InitialInboundConnection) delegate.get(connection);
        MinecraftConnection minecraftConnection = initial.getConnection();
        Channel channel = minecraftConnection.getChannel();

        if (LimboJoinProfiler.PROFILE_JOINS) {
            LimboJoinProfiler.update(channel, ConnectionStage.SERVER_PRE_LOGIN, event.getResult().toString());

            if (!event.getResult().isAllowed())
                return;
        }

        String name = this.util.getCorrectUsername(channel, event.getUsername());

        UUID uuid = event.getUniqueId();
        PersistentUserData data = UserFileManager.get(name);

        boolean isBedrock = this.util.isBedrock(channel);
        boolean bedrockPremium = isBedrock && this.authorizeLinked && this.util.isLinked(channel);

        if (bedrockPremium) {
            if (data == null)
                PersistentUserData.createFromPremiumInfo(name, ip, PremiumData.createNew(this.util.getLinkedJavaUUID(channel)));//should never be null

            boolean joinedRegistered = data != null;
            LoginInfo.set(channel, joinedRegistered, joinedRegistered ? LoginVerdict.LOGIN_PREMIUM : LoginVerdict.REGISTER_PREMIUM);

            continuation.resume();
            return;
        }

        //very important to skip any encryption/changes on floodgate connections
        if (isBedrock) {
            continuation.resume();
            return;
        }

        //we'll be the ones doing the premium auth if anything
        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());

        var version = Version.of(minecraftConnection.getProtocolVersion().getProtocol()).getClientVersion();

        //todo: rethink if we actually need to do this
        Consumer<PremiumData> consumer = newPremiumData -> {
            this.onPreLogin0(channel, data, minecraftConnection, name, name, newPremiumData, version, continuation);
        };

        if (data != null)
            PremiumSetting.performPremiumCheck(channel, data, name, uuid, version, consumer);
        else
            PremiumSetting.performPremiumCheckNullData(channel, name, uuid, version, consumer);
    }

    private void onPreLogin0(Channel channel, PersistentUserData data, MinecraftConnection minecraftConnection, String packetUsername,
                             String serverUsername, PremiumData newPremiumData, ClientVersion version, Continuation continuation) {
        boolean performPremiumCheck = newPremiumData.getStatus().isPremium();
        if (!performPremiumCheck && !EncryptionSetting.enableEncryption(version)) {
            continuation.resume();
            return;
        }

        //Premium Auth

        //the player should auth if the encryption request stems from premium auth req
        boolean shouldAuthenticate = performPremiumCheck;
        ClientPublicKey publicKey = PacketEventListener.publicKeys.remove(channel);
        byte[] token = EncryptionUtil.generateVerifyToken();
        EncryptionData encryptionData = new EncryptionData(packetUsername, serverUsername, token, publicKey, newPremiumData.premiumUUID(), shouldAuthenticate);

        WrapperLoginServerEncryptionRequest newPacket = new WrapperLoginServerEncryptionRequest("", keyPair.getPublic(), token, shouldAuthenticate);
        PacketEvents.getAPI().getProtocolManager().sendPacketSilently(channel, newPacket);

        LimboJoinProfiler.update(channel, ConnectionStage.SERVER_ENCRYPTION_REQUEST_SENT);

        preLoginContinuations.put(channel, new EncryptionInfo(encryptionData, continuation, minecraftConnection, newPremiumData, data));
    }
}