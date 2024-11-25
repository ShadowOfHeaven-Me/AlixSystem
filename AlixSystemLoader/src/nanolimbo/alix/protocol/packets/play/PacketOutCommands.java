package nanolimbo.alix.protocol.packets.play;

import nanolimbo.alix.commands.LimboCommand;
import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.registry.Version;

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