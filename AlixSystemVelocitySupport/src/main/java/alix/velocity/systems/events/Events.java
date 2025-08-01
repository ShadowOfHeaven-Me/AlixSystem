package alix.velocity.systems.events;


import alix.common.connection.filters.AntiVPN;
import alix.common.connection.filters.ConnectionManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.login.LoginVerdict;
import alix.common.login.premium.ClientPublicKey;
import alix.common.login.premium.EncryptionData;
import alix.common.login.premium.EncryptionUtil;
import alix.common.login.premium.PremiumSetting;
import alix.common.messages.Messages;
import alix.common.reflection.CommonReflection;
import alix.common.utils.AlixCache;
import alix.common.utils.floodgate.GeyserUtil;
import alix.velocity.server.AlixVelocityLimbo;
import alix.velocity.systems.events.premium.EncryptionInfo;
import alix.velocity.systems.packets.PacketEventListener;
import alix.velocity.utils.user.UserManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import com.google.common.cache.Cache;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import io.netty.channel.Channel;
import ua.nanit.limbo.connection.login.LoginInfo;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.registry.Version;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import static alix.common.login.premium.PremiumVerifier.keyPair;
import static alix.common.utils.config.ConfigProvider.config;

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

    @Subscribe(order = PostOrder.FIRST)
    public void onInitialServer(PlayerChooseInitialServerEvent event, Continuation continuation) {
        //Main.logInfo("onInitialServer=" + event.getPlayer());
        ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        var data = UserFileManager.get(player.getUsername());
        var channel = player.getConnection().getChannel();

        boolean bedrockPremium = this.authorizeLinked && this.util.isLinked(channel);

        //*should* be safe
        boolean premium = bedrockPremium || this.authorizePremium && data != null && data.getPremiumData().getStatus().isPremium();
        if (premium) {
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

    public static final Map<Channel, EncryptionInfo> preLoginContinuations;

    static {
        Cache<Channel, EncryptionInfo> cache = AlixCache.newBuilder().weakKeys().maximumSize(300).build();
        preLoginContinuations = cache.asMap();
    }

    private static final Field delegate = CommonReflection.getDeclaredFieldAccessible(LoginInboundConnection.class, "delegate");

    public static final PacketSnapshot
            //alreadyConnectingPacket = PacketPlayOutDisconnect.snapshot(Messages.get("already-connecting")),
            invalidNamePacket = PacketLoginDisconnect.snapshot(Messages.get("anti-bot-invalid-name-blocked")),
            preventFirstTimeJoinPacket = PacketLoginDisconnect.snapshot(ConnectionManager.preventFirstTimeJoinMessage),
            maxTotalAccountsPacket = PacketLoginDisconnect.snapshot(GeoIPTracker.maxAccountsReached),
            vpnDetectedPacket = PacketLoginDisconnect.snapshot(AntiVPN.antiVpnMessage);

    private final boolean assumePremiumWhenNoSuggestion = config.getBoolean("require-premium-when-no-suggestion");

    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        //Main.logInfo("DisconnectEvent=" + event.getPlayer() + " " + event.getLoginStatus());
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(PreLoginEvent event, Continuation continuation) throws IllegalAccessException {
        //Main.logInfo("onPreLogin=" + event.getResult() + " " + event.getUsername() + " " + event.getUniqueId());
        if (!event.getResult().isAllowed()) return;
        InetAddress ip = event.getConnection().getRemoteAddress().getAddress();

        LoginInboundConnection connection = (LoginInboundConnection) event.getConnection();
        InitialInboundConnection initial = (InitialInboundConnection) delegate.get(connection);
        MinecraftConnection minecraftConnection = initial.getConnection();
        Channel channel = minecraftConnection.getChannel();

        String name = this.util.getCorrectUsername(channel, event.getUsername());
        UUID uuid = event.getUniqueId();
        PersistentUserData data = UserFileManager.get(name);

        boolean bedrockPremium = this.authorizeLinked && this.util.isLinked(channel);

        if (bedrockPremium) {
            if (data == null)
                PersistentUserData.createFromPremiumInfo(name, ip, PremiumData.createNew(this.util.getLinkedJavaUUID(channel)));//should never be null

            boolean joinedRegistered = data != null;
            LoginInfo.set(channel, joinedRegistered, joinedRegistered ? LoginVerdict.LOGIN_PREMIUM : LoginVerdict.REGISTER_PREMIUM);

            continuation.resume();
            return;
        }

        PremiumData newPremiumData;
        boolean performPremiumCheck;// = data != null ? performPremiumCheck(data) : performPremiumCheckNullData(name);

        var version = Version.of(minecraftConnection.getProtocolVersion().getProtocol()).getClientVersion();

        if (data != null) {
            performPremiumCheck = PremiumSetting.performPremiumCheck(data, name, uuid, null, version);
            newPremiumData = data.getPremiumData();
        } else {
            newPremiumData = PremiumSetting.performPremiumCheckNullData(name, uuid, null, version);
            performPremiumCheck = newPremiumData.getStatus().isPremium();
        }

        //InetAddress ip = connection.getRemoteAddress().getAddress();

        //Already done in VelocityLimboIntegration
        /*switch (AlixCommonHandler.getPreLoginVerdict(ip, name, name, data, isPremium)) {
            case ALLOWED:
                break;
            case DISALLOWED_INVALID_NAME:
                PacketUtils.closeWith(channel, version, invalidNamePacket);
                return;
            case DISALLOWED_MAX_ACCOUNTS_REACHED:
                PacketUtils.closeWith(channel, version, maxTotalAccountsPacket);
                return;
            case DISALLOWED_PREVENT_FIRST_JOIN:
                PacketUtils.closeWith(channel, version, preventFirstTimeJoinPacket);
                return;
            case DISALLOWED_VPN_DETECTED:
                PacketUtils.closeWith(channel, version, vpnDetectedPacket);
                return;
            default:
                throw new AlixError();
        }*/

        /*for (VelocityConnectionFilter filter : filters)
            if (filter.disallowJoin(ip, name)) {
                event.setResult(filter.getResult());
                return;
            }*/

        if (!performPremiumCheck) {
            continuation.resume();
            return;
        }

        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());

        //Premium Auth

        ClientPublicKey publicKey = PacketEventListener.publicKeys.remove(channel);
        byte[] token = EncryptionUtil.generateVerifyToken();
        EncryptionData encryptionData = new EncryptionData(name, name, token, publicKey, newPremiumData.premiumUUID());

        WrapperLoginServerEncryptionRequest newPacket = new WrapperLoginServerEncryptionRequest("", keyPair.getPublic(), token);
        PacketEvents.getAPI().getProtocolManager().sendPacketSilently(channel, newPacket);

        preLoginContinuations.put(channel, new EncryptionInfo(encryptionData, continuation, minecraftConnection, newPremiumData, data));
    }
}