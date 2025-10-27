package ua.nanit.limbo.integration;

import alix.common.utils.AlixCache;
import alix.common.utils.floodgate.GeyserUtil;
import com.google.common.cache.Cache;
import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class LimboIntegration<T extends ClientConnection> {

    protected static GeyserUtil GEYSER_UTIL;
    protected static final Map<String, InetAddress> completedCaptchaCache;
    //private static final Map<InetAddress, String> ;

    static {
        Cache<String, InetAddress> cache = AlixCache.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(300).build();
        completedCaptchaCache = cache.asMap();
    }
    //Netty integration
    //void invokeSilentServerChannelRead(Channel channel);

    /*default T newConnection(Channel channel, LimboServer server) {
        return (T) new ClientConnection(channel, server);
    }*/


    public final void setHasCompletedCaptcha(InetAddress address, String name) {
        completedCaptchaCache.put(name, address);
    }

    /*public static boolean hasCompletedCaptcha(InetAddress address) {
        return completedCaptchaCache.get(address) != null;
    }

    public static boolean hasCompletedCaptcha(Channel channel) {
        return getCompletedCaptchaName(channel) != null;
    }*/
//Captcha
    @Nullable
    public static boolean hasCompletedCaptcha(Channel channel, String name) {
        /*if (GEYSER_UTIL.isBedrock(channel))
            return true;*/

        var ip = ((InetSocketAddress) channel.remoteAddress()).getAddress();
        return ip.equals(completedCaptchaCache.get(name));
    }

    public abstract T newConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state);


    //Packets
    public abstract void onHandshake(T connection, PacketHandshake handshake);

    public abstract PreLoginResult onLoginStart(T connection, PacketLoginStart loginStart, boolean[] recode);

    //Utils
    public abstract GeyserUtil geyserUtil();

    //Config
    public int getCompressionThreshold() {
        return 128;
    }

    public boolean isTransferAccepted() {
        return false;
    }

    //floodgate with compression disabled is present
    public boolean isFloodgateNoCompressionPresent() {
        return false;
    }

    public void fireCustomPayloadEvent(T connection, String channel, byte[] data) {
    }

    //Commands
    /*CommandHandler<T> createCommandHandler();

    //Transfer

    String getServerIP();

    int getPort();*/
}