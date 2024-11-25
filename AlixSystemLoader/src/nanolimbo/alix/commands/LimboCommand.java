package nanolimbo.alix.commands;

import io.netty.buffer.ByteBuf;
import nanolimbo.alix.protocol.PacketSnapshot;

import java.util.List;

public interface LimboCommand {

    ByteBuf getEncoded();

    PacketSnapshot getPacketSnapshot();

    static LimboCommand construct(List<String> aliases, String arg1Name) {
        return AbstractLimboCommand.construct0(aliases, arg1Name);
    }

    static LimboCommand construct(List<String> aliases, String arg1Name, String arg2Name) {
        return AbstractLimboCommand.construct0(aliases, arg1Name, arg2Name);
    }
}