package ua.nanit.limbo.commands;

import alix.common.packets.command.CustomCommand;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

import java.util.List;

public interface LimboCommand {

    void encode(ByteMessage msg, Version version);

    PacketSnapshot getPacketSnapshot();

    static LimboCommand construct(List<String> aliases, String arg1Name) {
        return LimboCommandImpl.construct0(aliases, arg1Name);
    }

    static LimboCommand construct(List<String> aliases, String arg1Name, String arg2Name) {
        return LimboCommandImpl.construct0(aliases, arg1Name, arg2Name);
    }

    static LimboCommand construct(List<String> aliases, String arg1Name, String arg2Name, String arg3Name) {
        return LimboCommandImpl.construct0(aliases, arg1Name, arg2Name, arg3Name);
    }

    static LimboCommand constructMultiCommand(List<CustomCommand> commands) {
        return new LimboMultiCommandImpl(commands);
    }
}