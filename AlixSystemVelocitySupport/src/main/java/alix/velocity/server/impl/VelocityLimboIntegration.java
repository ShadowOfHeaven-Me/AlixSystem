package alix.velocity.server.impl;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.login.premium.ClientPublicKey;
import alix.common.login.premium.PremiumUtils;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonHandler;
import alix.common.utils.floodgate.GeyserUtil;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import alix.velocity.Main;
import alix.velocity.server.impl.user.VelocityClientConnection;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.ChannelMessageSink;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.messages.PluginMessageEncoder;
import com.velocitypowered.proxy.VelocityServer;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.jetbrains.annotations.NotNull;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.util.UUID;
import java.util.function.Function;

import static alix.velocity.systems.events.Events.*;

public final class VelocityLimboIntegration extends LimboIntegration<VelocityClientConnection> {

    public static final AttributeKey<UUID> JOINED_UUID = AttributeKey.newInstance("alix:joined-uuid");
    private final GeyserUtil geyserUtil = Main.INSTANCE.util;

    @Override
    public void fireCustomPayloadEvent(VelocityClientConnection connection, String channel, byte[] data) {
        AlixScheduler.async(() -> {
            VelocityServer server = (VelocityServer) Main.PLUGIN.getServer();
            Player player = server.getPlayer(connection.getUsername()).orElseThrow(() -> new AlixException("How"));
            server.getEventManager().fireAndForget(new PluginMessageEvent(player, ArbitrarySink.SINK, MinecraftChannelIdentifier.from(channel), data));
        });
    }

    private static final class ArbitrarySink implements ChannelMessageSink {

        private static final ArbitrarySink SINK = new ArbitrarySink();

        private ArbitrarySink() {
        }

        @Override
        public boolean sendPluginMessage(@NotNull ChannelIdentifier identifier, byte @NotNull [] data) {
            return false;
        }

        @Override
        public boolean sendPluginMessage(@NotNull ChannelIdentifier identifier, @NotNull PluginMessageEncoder dataEncoder) {
            return false;
        }
    }

    @Override
    public int getCompressionThreshold() {
        return Main.PLUGIN.getServer().getConfiguration().getCompressionThreshold();
    }

    @Override
    public VelocityClientConnection newConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state) {
        return new VelocityClientConnection(channel, server, state);
    }

    @Override
    public void onHandshake(VelocityClientConnection connection, PacketHandshake handshake) {
    }

    @Override
    public boolean isFloodgateNoCompressionPresent() {
        return this.geyserUtil.isFloodgatePresent();
    }

    public static UUID getLoginAssignedUUID(Channel channel) {
        return channel.hasAttr(JOINED_UUID) ? channel.attr(JOINED_UUID).get() : null;
    }

    @Override
    public PreLoginResult onLoginStart(VelocityClientConnection connection, PacketLoginStart packet, boolean[] recode) {
        var channel = connection.getChannel();
        InetAddress ip = connection.getAddress().getAddress();
        String name = packet.getUsername();
        PersistentUserData data = UserFileManager.get(name);
        UUID uuid = packet.getUUID();

        if (uuid != null) channel.attr(JOINED_UUID).set(uuid);

        var suggestsStatus = PremiumUtils.suggestsStatus(uuid, ClientPublicKey.createKey(packet.getSignatureData()), connection.getRetrooperClientVersion());

        PremiumData premiumData = null;
        var isPremium = suggestsStatus.isPremium() && (premiumData = PremiumUtils.getOrRequestAndCacheData(data, name)).getStatus().isPremium();

        //String ip = event.getConnection().getRemoteAddress().getAddress().getHostAddress();

        switch (AlixCommonHandler.getPreLoginVerdict(ip, name, name, data, isPremium)) {
            case ALLOWED:
                break;
            case DISALLOWED_INVALID_NAME:
                connection.sendPacketAndClose(invalidNamePacket);
                return PreLoginResult.DISCONNECTED;
            case DISALLOWED_MAX_ACCOUNTS_REACHED:
                connection.sendPacketAndClose(maxTotalAccountsPacket);
                return PreLoginResult.DISCONNECTED;
            case DISALLOWED_PREVENT_FIRST_JOIN:
                connection.sendPacketAndClose(preventFirstTimeJoinPacket);
                return PreLoginResult.DISCONNECTED;
            case DISALLOWED_VPN_DETECTED:
                connection.sendPacketAndClose(vpnDetectedPacket);
                return PreLoginResult.DISCONNECTED;
            default:
                throw new AlixError();
        }

        Boolean hasCompletedCaptcha = null;

        //Intention hide
        boolean isTransfer = connection.getHandshakePacket().isTransfer();
        if (isTransfer && (hasCompletedCaptcha = hasCompletedCaptcha(ip, name))) {
            connection.getHandshakePacket().setLoginIntention();
            recode[0] = true;
        }

        //we cannot include `isPremium` here, because this would introduce a bypass
        return data != null || this.geyserUtil.isBedrock(channel) || (hasCompletedCaptcha != null ? hasCompletedCaptcha == Boolean.TRUE : hasCompletedCaptcha(ip, name)) ? PreLoginResult.CONNECT_TO_MAIN_SERVER : PreLoginResult.CONNECT_TO_LIMBO;
    }

    @Override
    public GeyserUtil geyserUtil() {
        return this.geyserUtil;
    }
}