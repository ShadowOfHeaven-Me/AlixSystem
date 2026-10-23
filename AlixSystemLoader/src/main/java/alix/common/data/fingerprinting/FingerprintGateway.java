package alix.common.data.fingerprinting;

import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigParams;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientResourcePackStatus;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;
import ua.nanit.limbo.protocol.registry.Version;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The user-facing entry point into device-fingerprinting ("Device PassKey"): both /account's "Create a
 * Device PassKey" login-setting item and the passkey option of the "Recover account?" login-time gui call
 * through here.
 */
public final class FingerprintGateway {

    public static final AttributeKey<PersistentUserData> USER_DATA_ATTR = AttributeKey.valueOf("alix:persistent-user-data");
    static final Map<Channel, FingerprintSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    /**
     * @param channel  the connection to fingerprint - a real backend player's channel when creating a new
     *                 passkey from /account, or a pre-login ClientConnection's channel when verifying one
     *                 during "Recover account?"
     * @param callback completed with true once the device's fingerprint was successfully obtained AND (for
     *                 a verification call) matches what's already on file, false otherwise
     */
    public static void sendFingerprintingPacks(Channel channel, Consumer<Boolean> callback) {
        if (!ConfigParams.fingerprintingEnabled || channel == null || !channel.isOpen()) {
            if (callback != null) callback.accept(false);
            return;
        }

        PersistentUserData data = getData(channel);
        if (data == null) {
            if (callback != null) callback.accept(false);
            return;
        }

        FingerprintServer.init();

        Version version = getVersion(channel);
        if (version.less(Version.V1_20_3)) {
            sendCannotFingerprintMessage(channel, CipherHandler.encryptionFor(channel), version);
            if (callback != null) callback.accept(false);
            return;
        }

        String host = getHost(channel);
        int port = ConfigParams.fingerprintingPort;
        String sessionName = data.getName().toLowerCase(Locale.ROOT);

        FingerprintSession old = ACTIVE_SESSIONS.remove(channel);
        if (old != null) {
            old.abort();
        }

        FingerprintSession session = new FingerprintSession(channel, sessionName, host, port, version, data, callback);
        ACTIVE_SESSIONS.put(channel, session);
        FingerprintHandler.MAP.put(sessionName, session);

        channel.closeFuture().addListener(f -> {
            session.abort();
            cleanupSession(channel, sessionName);
        });

        session.start();
    }

    public static void onResourcePackStatus(Channel channel, UUID packId, WrapperPlayClientResourcePackStatus.Result result) {
        if (channel == null) return;
        FingerprintSession session = ACTIVE_SESSIONS.get(channel);
        if (session != null) {
            session.onResourcePackStatus(packId, result);
        }
    }

    static void cleanupSession(Channel channel, String sessionName) {
        ACTIVE_SESSIONS.remove(channel);
        FingerprintHandler.MAP.remove(sessionName);
    }

    public static void sendCannotFingerprintMessage(Channel channel, CipherHandler cipher, Version version) {
        var packet = PacketPlayOutMessage.withMessage(Messages.getWithPrefix("device-fingerprint-cannot-fingerprint"));
        PacketUtils.writeAndFlush(channel, version, packet, cipher);
    }

    public static PersistentUserData getData(Channel channel) {
        if (channel.hasAttr(USER_DATA_ATTR)) {
            var data = channel.attr(USER_DATA_ATTR).get();
            if (data != null) return data;
        }
        var duplex = channel.pipeline().get(PacketDuplexHandler.class);
        if (duplex != null && duplex.getConnection().getVerifyState() instanceof LoginState ls) {
            return ls.data;
        }
        return null;
    }

    public static String getHost(Channel channel) {
        if (channel.hasAttr(PacketHandshake.HANDSHAKE_HOST)) {
            String host = channel.attr(PacketHandshake.HANDSHAKE_HOST).get();
            if (host != null && !host.isEmpty()) return host;
        }
        var duplex = channel.pipeline().get(PacketDuplexHandler.class);
        if (duplex != null && duplex.getConnection().getHandshakePacket() != null) {
            String host = duplex.getConnection().getHandshakePacket().getExtractedHost();
            if (host != null && !host.isEmpty()) return host;
        }
        if (channel.localAddress() instanceof InetSocketAddress local) {
            return local.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    public static Version getVersion(Channel channel) {
        var duplex = channel.pipeline().get(PacketDuplexHandler.class);
        if (duplex != null) {
            return duplex.getVersion();
        }
        try {
            var peVersion = PacketEvents.getAPI().getPlayerManager().getClientVersion(channel);
            if (peVersion != null) {
                return Version.of(peVersion.getProtocolVersion());
            }
        } catch (Throwable ignored) {
        }
        return Version.getMax();
    }

    private FingerprintGateway() {
    }
}
