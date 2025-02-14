package ua.nanit.limbo.integration;

import io.netty.channel.Channel;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.util.function.Function;

public interface LimboIntegration<T extends ClientConnection> {

    //Netty integration
    //void invokeSilentServerChannelRead(Channel channel);

    /*default T newConnection(Channel channel, LimboServer server) {
        return (T) new ClientConnection(channel, server);
    }*/

    T newConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state);

    //Captcha
    boolean hasCompletedCaptcha(String name, Channel channel);

    void completeCaptcha(T conn);

    void setHasCompletedCaptcha(InetAddress address, String name);

    //Packets
    void onHandshake(T connection, PacketHandshake handshake);

    PreLoginResult onLoginStart(T connection, PacketLoginStart loginStart, boolean[] recode);

    default int getCompressionThreshold() {
        return 128;
    }

    //Commands
    /*CommandHandler<T> createCommandHandler();

    //Transfer

    String getServerIP();

    int getPort();*/
}