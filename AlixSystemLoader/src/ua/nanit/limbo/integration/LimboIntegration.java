package ua.nanit.limbo.integration;

import io.netty.channel.Channel;
import ua.nanit.limbo.commands.CommandHandler;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.protocol.packets.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;

public interface LimboIntegration<T extends ClientConnection> {

    //Netty integration
    void invokeSilentServerChannelRead(Channel channel);

    T newConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder);

    //Captcha
    boolean hasCompletedCaptcha(String name, Channel channel);

    void completeCaptcha(T conn);

    //Packets
    void onHandshake(T connection, PacketHandshake handshake);

    PreLoginResult onLoginStart(T connection, PacketLoginStart loginStart);

    //Commands
    CommandHandler<T> createCommandHandler();

    //Transfer

    String getServerIP();

    int getPort();

    void setHasCompletedCaptcha(InetAddress address, String name);
}