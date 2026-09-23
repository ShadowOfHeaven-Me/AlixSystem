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
        if (!conn.getVerifyState().isLoginState()) return true;
        // Pre-1.19 clients send commands as chat; 1.19+ clients use the dedicated command packets instead.
        return conn.getClientVersion().moreOrEqual(Version.V1_19);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        String cmd = this.wrapper().getMessage();
        //Log.error("CMD: '" + cmd + "'");
        if (cmd.isEmpty() || cmd.charAt(0) != '/') return;
        //handleCommand() itself enforces which commands are allowed while a login GUI is active, so every
        //command source (this legacy pre-1.19 chat path included) is gated identically in one place.
        ((LoginState) conn.getVerifyState()).handleCommand(cmd);
    }

    public static String[] getArgs(String cmd) {
        String[] split = cmd.split(" ");

        return Arrays.copyOfRange(split, 1, split.length);
    }
}