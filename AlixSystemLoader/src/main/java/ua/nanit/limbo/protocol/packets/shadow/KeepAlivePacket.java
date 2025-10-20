package ua.nanit.limbo.protocol.packets.shadow;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketOutConfigKeepAlive;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketPlayOutKeepAlive;

public final class KeepAlivePacket {

    private final PacketSnapshot config, play;

    private KeepAlivePacket(int id) {
        this.config = new PacketOutConfigKeepAlive(id).toSnapshot();
        this.play = new PacketPlayOutKeepAlive(id).toSnapshot();
    }

    public PacketSnapshot getPacket(ClientConnection connection) {
        return switch (connection.getDecoderState()) {
            case CONFIGURATION -> this.config;
            case PLAY -> this.play;
            default -> null;
        };
    }

    public void writeAndFlushPacket(ClientConnection connection) {
        var packet = this.getPacket(connection);
        if (packet == null) {
            //UnsafeCloseFuture.unsafeClose(connection.getChannel());
            return;
            //throw new AlixError("connection.getState()=" + connection.getState());
        }
        connection.writeAndFlushPacket(packet);
    }

    public static KeepAlivePacket sex(int id) {
        return new KeepAlivePacket(id);
    }
}