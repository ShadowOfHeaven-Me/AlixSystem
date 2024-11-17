package shadow.virtualization;

import alix.common.utils.other.annotation.OptimizationCandidate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.Channel;
import nanolimbo.alix.LimboIntegration;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.PacketHandshake;
import nanolimbo.alix.protocol.packets.login.PacketLoginStart;
import shadow.systems.login.captcha.Captcha;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

//https://wiki.vg/Protocol_FAQ
public final class LimboServerIntegration implements LimboIntegration {

    //with caffeine
    @OptimizationCandidate
    private final Cache<String, InetAddress> completedCaptchaCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    @Override
    public boolean hasCompletedCaptcha(String name, Channel channel) {
        return Captcha.hasCompletedCaptcha(name, channel) || ((InetSocketAddress) channel.remoteAddress()).getAddress().equals(this.completedCaptchaCache.getIfPresent(name));
    }

    @Override
    public void completeCaptcha(String name, Channel channel) {

    }

    @Override
    public void onHandshake(ClientConnection connection, PacketHandshake handshake) {

    }

    @Override
    public boolean onLoginStart(ClientConnection connection, PacketLoginStart loginStart) {
        return true;
    }
}
