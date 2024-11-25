package nanolimbo.alix.integration;

import io.netty.channel.Channel;
import nanolimbo.alix.commands.CommandHandler;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.connection.pipeline.PacketDuplexHandler;
import nanolimbo.alix.connection.pipeline.VarIntFrameDecoder;
import nanolimbo.alix.protocol.packets.PacketHandshake;
import nanolimbo.alix.protocol.packets.login.PacketLoginStart;
import nanolimbo.alix.server.LimboServer;

public interface LimboIntegration<T extends ClientConnection> {

    T newConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder);

    //Captcha

    boolean hasCompletedCaptcha(String name, Channel channel);

    void completeCaptcha(T conn);

    //Packets

    void onHandshake(T connection, PacketHandshake handshake);

    PreLoginResult onLoginStart(T connection, PacketLoginStart loginStart);

    //Commands

    CommandHandler<T> createCommandHandler();

}