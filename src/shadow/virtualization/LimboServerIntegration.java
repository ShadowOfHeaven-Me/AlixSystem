package shadow.virtualization;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import io.netty.channel.Channel;
import nanolimbo.alix.commands.CommandHandler;
import nanolimbo.alix.connection.pipeline.PacketDuplexHandler;
import nanolimbo.alix.connection.pipeline.VarIntFrameDecoder;
import nanolimbo.alix.integration.LimboIntegration;
import nanolimbo.alix.integration.PreLoginResult;
import nanolimbo.alix.protocol.packets.PacketHandshake;
import nanolimbo.alix.protocol.packets.login.PacketLoginStart;
import nanolimbo.alix.server.LimboServer;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.netty.AlixInterceptor;
import shadow.virtualization.commands.LimboCommandHandler;

//https://wiki.vg/Protocol_FAQ
public final class LimboServerIntegration implements LimboIntegration<LimboConnection> {

    //with caffeine
    //@OptimizationCandidate
    //private final Cache<String, InetAddress> completedCaptchaCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

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

    @Override
    public PreLoginResult onLoginStart(LimboConnection connection, PacketLoginStart packet) {
        String name = packet.getUsername();
        PersistentUserData data = UserFileManager.get(name);
        //AlixChannelHandler.onLoginStart()

        return PreLoginResult.CONNECT_TO_LIMBO;
    }

    @Override
    public CommandHandler<LimboConnection> createCommandHandler() {
        return new LimboCommandHandler();
    }
}
