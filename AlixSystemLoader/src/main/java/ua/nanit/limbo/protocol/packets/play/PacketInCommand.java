package ua.nanit.limbo.protocol.packets.play;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

import static ua.nanit.limbo.protocol.packets.play.PacketInCommandUnsigned.getArgs;

public final class PacketInCommand extends InRetrooperPacket<WrapperPlayClientChatCommand> {

    public PacketInCommand() {
        super(WrapperPlayClientChatCommand.class);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        if (!(conn.getVerifyState() instanceof LoginState)) return;

        String cmd = this.wrapper().getCommand();
        ((LoginState) conn.getVerifyState()).handleCommand(getArgs(cmd));
    }
}