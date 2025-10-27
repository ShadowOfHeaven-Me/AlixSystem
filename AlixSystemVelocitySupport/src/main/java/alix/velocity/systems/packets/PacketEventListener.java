package alix.velocity.systems.packets;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.VerifiedCache;
import alix.common.login.LoginVerdict;
import alix.common.login.premium.*;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.velocity.Main;
import alix.velocity.server.AlixVelocityLimbo;
import alix.velocity.systems.events.Events;
import alix.velocity.utils.user.UserManager;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketLoginReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerPluginMessage;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.velocitypowered.api.event.Continuation;
import io.netty.channel.Channel;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.login.LoginInfo;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.packets.play.payload.PacketPlayOutPluginMessage;
import ua.nanit.limbo.protocol.registry.Version;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static alix.common.utils.config.ConfigProvider.config;

public final class PacketEventListener extends PacketListenerAbstract {

    private static final PacketSnapshot
            illegalEncryptionState = PacketLoginDisconnect.snapshot("§cIllegal encryption state"),
            invalidNonce = PacketLoginDisconnect.snapshot("§cInvalid nonce"),
            cannotDecryptSharedSecret = PacketLoginDisconnect.snapshot("§cCannot decrypt shared secret"),
            invalidSession = PacketLoginDisconnect.snapshot("§cInvalid session"),
            cannotVerifySession = PacketLoginDisconnect.snapshot("§cCannot verify session"),
            couldNotEnableEncryption = PacketLoginDisconnect.snapshot("§cCouldn't enable encryption");
    /*internalErrorEncryption = PacketLoginDisconnect.snapshot("§cInternal error (Encryption)"),
    cachedNameDoesNotMatch = PacketLoginDisconnect.snapshot("§cCached name does not match");*/
    public static final Map<Channel, ClientPublicKey> publicKeys = new ConcurrentHashMap<>();
    private static final KeyPair keyPair = PremiumVerifier.keyPair;
    private static final boolean debugPackets = NanoLimbo.debugPackets;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (debugPackets) Main.logInfo("IN: " + event.getPacketType());
        /*event.getPostTasks().add(() -> {
            Main.logInfo("IS: " + event.getPacketType() + " CANCELLED " + event.isCancelled());
        });*/
        Channel channel = (Channel) event.getChannel();
        var user = event.getUser();

        /*if (event.getPacketType() == PacketType.Status.Client.REQUEST) {
            channel.pipeline().addAfter("pe-decoder-alixsystem", "seks", new ChannelDuplexHandler() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    Main.logInfo("CO TO WYJEBAŁO");
                    cause.printStackTrace();
                    //super.exceptionCaught(ctx, cause);
                }
            });

            Main.logInfo("PIP=" + channel.pipeline().names());
            channel.closeFuture().addListener(f -> {
                Main.logInfo("CLOSED");
                new Exception().printStackTrace();
            });
        }*/
        if (event instanceof PacketLoginReceiveEvent loginEvent) {
            if (NanoLimbo.INTEGRATION.geyserUtil().isBedrock(channel))
                return;
            switch (loginEvent.getPacketType()) {
                case LOGIN_START: {
                    var key = ClientPublicKey.createKey(new WrapperLoginClientLoginStart(event).getSignatureData().orElse(null));
                    if (key != null) publicKeys.put(channel, key);
                    return;
                }
                case ENCRYPTION_RESPONSE: {
                    var packet = new WrapperLoginClientEncryptionResponse(event);
                    event.setCancelled(true);
                    AlixScheduler.asyncBlocking(() -> this.onEncryptionResponse(packet, user, channel));
                }
            }
            return;
        }
        if (event instanceof PacketPlayReceiveEvent playEvent) {
            var alixUser = UserManager.getVerified(user.getUUID());
            if (alixUser == null) return;//...how?

            alixUser.getDuplexProcessor().onReceive(playEvent);

            switch (playEvent.getPacketType()) {
                case CLICK_WINDOW: {

                    var gui = alixUser.gui;
                    if (gui == null) return;

                    event.setCancelled(true);
                    var wrapper = new WrapperPlayClientClickWindow(event);
                    gui.onClick(wrapper);
                    //gui.getGUI().spoofItems();
                    return;
                }
                case CLOSE_WINDOW: {
                    //Main.logInfo("§cCLOSE_WINDOW");
                    var gui = alixUser.gui;
                    if (gui != null) {
                        event.setCancelled(true);
                        alixUser.gui = null;
                    }
                    //var wrapper = new WrapperPlayClientCloseWindow(event);
                }
            }
        }
    }

    private void onEncryptionResponse(WrapperLoginClientEncryptionResponse packet, User user, Channel channel) {
        byte[] sharedSecret = packet.getEncryptedSharedSecret();
        var encryptionInfo = Events.preLoginContinuations.get(channel);
        var continuation = encryptionInfo.continuation();

        if (encryptionInfo == null) {
            //we weren't expecting this packet
            this.disconnectWith(user, illegalEncryptionState);
            return;
        }

        EncryptionData encryptionData = encryptionInfo.encryptionData();
        PersistentUserData data = encryptionInfo.data();

        byte[] expectedToken = encryptionData.token();
        PrivateKey privateKey = keyPair.getPrivate();

        SecretKey loginKey;

        try {
            loginKey = EncryptionUtil.decryptSharedKey(privateKey, sharedSecret);
        } catch (Exception securityEx) {
            //we cannot enable encryption without this one
            this.disconnectWith(user, cannotDecryptSharedSecret);
            return;
        }


        //enable encryption once we know that the player is expecting the connection to be encrypted

        channel.eventLoop().execute(() -> {
            try {
                encryptionInfo.minecraftConnection().enableEncryption(loginKey.getEncoded());
            } catch (GeneralSecurityException e) {
                this.disconnectWith(user, couldNotEnableEncryption);
                AlixCommonUtils.logException(e);
            }
        });

        if (!PremiumVerifier.verifyNonce(packet, encryptionData.publicKey(), expectedToken, user.getClientVersion().toServerVersion())) {
            if (!encryptionData.shouldAuthenticate()) {
                //wtf do we do now
                this.disconnectWith(user, invalidNonce);
                return;
            }

            this.onInvalidAuth(user, continuation, invalidNonce);
            return;//did they forget a return here? - https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/PaperListeners.java#L270
        }
        if (!encryptionData.shouldAuthenticate()) {
            continuation.resume();
            return;
        }

        //Verify session

        String serverId = EncryptionUtil.getServerIdHashString("", loginKey, keyPair.getPublic());
        String packetUsername = encryptionData.packetUsername();
        String serverUsername = encryptionData.serverUsername();
        InetSocketAddress address = user.getAddress();

        try {
            //make sure to remove the name char prefix (currently disabled)
            if (PremiumVerifier.hasJoined(packetUsername, serverId, address.getAddress())) {

                PremiumData premiumData = encryptionInfo.premiumData();
                if (!premiumData.getStatus().isPremium()) premiumData = PremiumUtils.getPremiumData(packetUsername);

                if (data == null)
                    PersistentUserData.createFromPremiumInfo(serverUsername, user.getAddress().getAddress(), premiumData);

                boolean joinedRegistered = encryptionData != null;
                LoginInfo.set(channel, joinedRegistered, joinedRegistered ? LoginVerdict.LOGIN_PREMIUM : LoginVerdict.REGISTER_PREMIUM);

                VerifiedCache.verify(serverUsername, user);
                continuation.resume();
                //Main.logInfo("CONTINUATION TRUE");
                //receiveFakeStartPacket(packetUsername, encryptionData.publicKey(), user.getChannel(), encryptionData.uuid());
            } else {
                this.onInvalidAuth(user, continuation, invalidSession);
            }
        } catch (IOException e) {
                        /*if (e instanceof SocketTimeoutException) {
                            Main.logWarning("Session verification timed out (5 seconds) for " + serverUsername);
                        }*/
            //todo: see if this correct
            this.onInvalidAuth(user, continuation, cannotVerifySession);
        }
    }

    private final boolean
            assumeNonPremiumOnFailedAuth = config.getBoolean("assume-non-premium-if-auth-failed");
    //assignPremiumUUID = config.getBoolean("premium-uuid");

    private void onInvalidAuth(User user, Continuation continuation, PacketSnapshot invalidReason) {
        String name = user.getName();
        PersistentUserData data = UserFileManager.get(name);
        boolean registered = PersistentUserData.isRegistered(data);

        if (!registered && assumeNonPremiumOnFailedAuth) {
            Main.logInfo("Assuming the premium-nicknamed user " + name + " is non-premium!");

            if (data != null) data.setPremiumData(PremiumData.NON_PREMIUM);
            else PremiumDataCache.add(name, PremiumData.NON_PREMIUM);

            continuation.resume();
            return;
        }
        this.disconnectWith(user, invalidReason);
    }

    private void disconnectWith(User user, PacketSnapshot packet) {
        Channel channel = (Channel) user.getChannel();
        Runnable r = () -> PacketUtils.closeWith(channel, Version.of(user.getClientVersion().getProtocolVersion()), packet);

        if (channel.eventLoop().inEventLoop()) r.run();
        else channel.eventLoop().execute(r);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (debugPackets) Main.logInfo("OUT: " + event.getPacketType());
        Channel channel = (Channel) event.getChannel();
        var user = event.getUser();

        if (event.getPacketType() == PacketType.Configuration.Server.PLUGIN_MESSAGE) {
            var connection = AlixVelocityLimbo.LIMBO_CONNECTIONS.get(channel);
            if (connection == null || connection.isInConfigPhase()) return;

            var wrapper = new WrapperConfigServerPluginMessage(event);
            var limboPacket = new PacketPlayOutPluginMessage(new WrapperPlayServerPluginMessage(wrapper.getChannelName(), wrapper.getData()));

            //Main.logInfo("REFACTORED: " + event.getPacketType());
            connection.writeAndFlushPacket(limboPacket);
            event.setCancelled(true);
            return;
        }

        if (event instanceof PacketPlaySendEvent play) {
            /*switch (play.getPacketType()) {
                case SYSTEM_CHAT_MESSAGE -> {
                    var wrapper = new WrapperPlayServerSystemChatMessage(event);
                    Main.logInfo("WRAPPER=" + wrapper.getMessage());
                    event.setCancelled(true);
                    return;
                }
            }*/
            var alixUser = UserManager.getVerified(user.getUUID());
            if (alixUser == null) return;//uhhhhhhh
            alixUser.getDuplexProcessor().onSend(play);
        }
    }
}