package nanolimbo.alix;

import io.netty.channel.Channel;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.packets.PacketHandshake;
import nanolimbo.alix.protocol.packets.login.PacketLoginStart;

public interface LimboIntegration {

    //Captcha

    boolean hasCompletedCaptcha(String name, Channel channel);

    void completeCaptcha(String name, Channel channel);

    //Packets

    void onHandshake(ClientConnection connection, PacketHandshake handshake);

    //returns true if the connection is allowed, false if it was closed
    boolean onLoginStart(ClientConnection connection, PacketLoginStart loginStart);
}