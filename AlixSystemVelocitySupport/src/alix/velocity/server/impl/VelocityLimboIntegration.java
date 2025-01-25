package alix.velocity.server.impl;

import io.netty.channel.Channel;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;

public final class VelocityLimboIntegration implements LimboIntegration {

    @Override
    public ClientConnection newConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder) {
        return null;
    }

    @Override
    public boolean hasCompletedCaptcha(String name, Channel channel) {
        return false;
    }

    @Override
    public void completeCaptcha(ClientConnection conn) {

    }

    @Override
    public void onHandshake(ClientConnection connection, PacketHandshake handshake) {

    }

    @Override
    public PreLoginResult onLoginStart(ClientConnection connection, PacketLoginStart loginStart, boolean[] recode) {
        return null;
    }

    @Override
    public void setHasCompletedCaptcha(InetAddress address, String name) {

    }
}