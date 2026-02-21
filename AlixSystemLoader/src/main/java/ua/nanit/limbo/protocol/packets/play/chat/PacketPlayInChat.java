package ua.nanit.limbo.protocol.packets.play.chat;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

import java.util.Arrays;

public final class PacketPlayInChat extends InRetrooperPacket<WrapperPlayClientChatMessage> {

    public PacketPlayInChat() {
        super(WrapperPlayClientChatMessage.class);
    }

    @Override
    public boolean isSkippable(ClientConnection conn) {
        return conn.getClientVersion().moreOrEqual(Version.V1_19) || !(conn.getVerifyState().isLoginState())
               || ((LoginState) conn.getVerifyState()).gui != null;
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        String cmd = this.wrapper().getMessage();
        //Log.error("CMD: '" + cmd + "'");
        if (cmd.isEmpty() || cmd.charAt(0) != '/') return;
        ((LoginState) conn.getVerifyState()).handleCommand(getArgs(cmd));
    }

    public static String[] getArgs(String cmd) {
        String[] split = cmd.split(" ");

        return Arrays.copyOfRange(split, 1, split.length);
    }
}