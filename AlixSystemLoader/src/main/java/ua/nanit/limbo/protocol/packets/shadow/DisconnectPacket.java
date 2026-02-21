package ua.nanit.limbo.protocol.packets.shadow;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.UnsafeCloseFuture;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.login.PacketConfigDisconnect;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;

public final class DisconnectPacket {

    private final PacketSnapshot login, config, play;

    private DisconnectPacket(String reason) {
        this.login = PacketLoginDisconnect.snapshot(reason);
        this.config = PacketConfigDisconnect.snapshot(reason);
        this.play = PacketPlayOutDisconnect.snapshot(reason);
    }

    public void disconnect(ClientConnection connection) {
        var packet = switch (connection.getEncoderState()) {
            case HANDSHAKING, LOGIN -> this.login;
            case CONFIGURATION -> this.config;
            case PLAY -> this.play;
            case STATUS -> null;
        };
        if (packet == null) {
            UnsafeCloseFuture.unsafeClose(connection.getChannel());
            return;
            //throw new AlixError("connection.getState()=" + connection.getState());
        }
        connection.sendPacketAndClose(packet);
    }

    public static DisconnectPacket error(String error) {
        return withReason("§c" + error);
    }

    public static DisconnectPacket withReason(String reason) {
        return new DisconnectPacket(reason);
    }
}