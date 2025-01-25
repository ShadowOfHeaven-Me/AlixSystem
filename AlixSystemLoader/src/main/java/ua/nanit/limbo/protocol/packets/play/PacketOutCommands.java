package ua.nanit.limbo.protocol.packets.play;

import ua.nanit.limbo.commands.LimboCommand;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketOutCommands implements PacketOut {

    private LimboCommand command;

    public PacketOutCommands() {
    }

    public PacketOutCommands(LimboCommand command) {
        this.command = command;
    }

    public LimboCommand getCommand() {
        return command;
    }

    public void setCommand(LimboCommand command) {
        this.command = command;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeBytes(this.command.getEncoded());
    }
}