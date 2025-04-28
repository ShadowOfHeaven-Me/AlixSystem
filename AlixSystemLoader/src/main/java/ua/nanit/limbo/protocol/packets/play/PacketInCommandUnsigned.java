package ua.nanit.limbo.protocol.packets.play;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

import java.util.Arrays;

public final class PacketInCommandUnsigned extends InRetrooperPacket<WrapperPlayClientChatCommandUnsigned> {

    public PacketInCommandUnsigned() {
        super(WrapperPlayClientChatCommandUnsigned.class);
    }

    @Override
    public boolean isSkippable(ClientConnection conn) {
        return !(conn.getVerifyState() instanceof LoginState);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        String cmd = this.wrapper().getCommand();
        ((LoginState) conn.getVerifyState()).handleCommand(getArgs(cmd));
    }

    public static String[] getArgs(String cmd) {
        String[] split = cmd.split(" ");

        return Arrays.copyOfRange(split, 1, split.length);
    }
}