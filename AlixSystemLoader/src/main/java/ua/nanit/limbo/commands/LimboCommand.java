package ua.nanit.limbo.commands;

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.registry.Version;

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
}