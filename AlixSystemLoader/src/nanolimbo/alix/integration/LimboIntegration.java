package nanolimbo.alix.integration;

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

    PreLoginResult onLoginStart(ClientConnection connection, PacketLoginStart loginStart);
}