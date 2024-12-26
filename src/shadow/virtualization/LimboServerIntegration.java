package shadow.virtualization;

import alix.common.connection.filters.AntiVPN;
import alix.common.connection.filters.ConnectionManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.throwable.AlixError;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import shadow.systems.login.autoin.PremiumAccountCache;
import shadow.systems.login.autoin.premium.ClientPublicKey;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.netty.AlixChannelHandler;
import shadow.systems.netty.AlixInterceptor;
import shadow.virtualization.commands.LimboCommandHandler;
import ua.nanit.limbo.commands.CommandHandler;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//https://wiki.vg/Protocol_FAQ
public final class LimboServerIntegration implements LimboIntegration<LimboConnection> {

    //with caffeine
    //@OptimizationCandidate
    //private final Cache<String, InetAddress> completedCaptchaCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    @OptimizationCandidate
    private static final Map<InetAddress, String> completedCaptchaCache;
    //private static final Map<InetAddress, String> ;

    static {
        Cache<InetAddress, String> cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
        completedCaptchaCache = cache.asMap();
    }

    @Override
    public void setHasCompletedCaptcha(InetAddress address, String name) {
        completedCaptchaCache.put(address, name);
    }

    public static boolean hasCompletedCaptcha(InetAddress address) {
        return completedCaptchaCache.get(address) != null;
    }

    public static boolean hasCompletedCaptcha(Channel channel) {
        return getCompletedCaptchaName(channel) != null;
    }

    @Nullable
    public static String getCompletedCaptchaName(Channel channel) {
        return getCompletedCaptchaName(((InetSocketAddress) channel.remoteAddress()).getAddress());
    }

    @Nullable
    public static String getCompletedCaptchaName(InetAddress address) {
        return completedCaptchaCache.get(address);
    }

    @Nullable
    public static String removeCompletedCaptchaName(InetAddress address) {
        return completedCaptchaCache.remove(address);
    }

    @Override
    public void invokeSilentServerChannelRead(Channel channel) {
        AlixInterceptor.invokeSilentChannelRead(channel);
    }

    @Override
    public LimboConnection newConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder) {
        return new LimboConnection(channel, server, duplexHandler, frameDecoder);
    }

    @Override
    public boolean hasCompletedCaptcha(String name, Channel channel) {
        return Captcha.hasCompletedCaptcha(name, channel);// || ((InetSocketAddress) channel.remoteAddress()).getAddress().equals(this.completedCaptchaCache.getIfPresent(name));
    }

    @Override
    public void completeCaptcha(LimboConnection conn) {
    }

    @Override
    public void onHandshake(LimboConnection connection, PacketHandshake handshake) {
    }

    private static final PacketSnapshot
            //alreadyConnectingPacket = PacketPlayOutDisconnect.snapshot(Messages.get("already-connecting")),
            invalidNamePacket = PacketPlayOutDisconnect.snapshot(AlixChannelHandler.invalidNameMessage),
            preventFirstTimeJoinPacket = PacketPlayOutDisconnect.snapshot(ConnectionManager.preventFirstTimeJoinMessage),
            maxTotalAccountsPacket = PacketPlayOutDisconnect.snapshot(GeoIPTracker.maxAccountsReached),
            vpnDetectedPacket = PacketPlayOutDisconnect.snapshot(AntiVPN.antiVpnMessage);

    @Override
    public PreLoginResult onLoginStart(LimboConnection connection, PacketLoginStart packet) {
        String name = packet.getUsername();
        Channel channel = connection.getChannel();
        PersistentUserData data = UserFileManager.get(name);

        PremiumData premiumData = PremiumAccountCache.getCachedOrSet(name, ClientPublicKey.createKey(packet.getSignatureData()), packet.getUUID(), data);
        boolean isPremium = premiumData.getStatus().isPremium();

        switch (AlixChannelHandler.getPreLoginVerdict(channel, name, data, isPremium)) {
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

        return data != null || isPremium || name.equals(getCompletedCaptchaName(channel)) ? PreLoginResult.CONNECT_TO_MAIN_SERVER : PreLoginResult.CONNECT_TO_LIMBO;
    }

    @Override
    public CommandHandler<LimboConnection> createCommandHandler() {
        return new LimboCommandHandler();
    }

    @Override
    public String getServerIP() {
        return Bukkit.getServer().getIp();
    }

    @Override
    public int getPort() {
        return Bukkit.getPort();
    }
}
