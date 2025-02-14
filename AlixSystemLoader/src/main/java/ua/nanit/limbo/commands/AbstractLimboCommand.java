package ua.nanit.limbo.commands;

import alix.common.packets.command.CommandsWrapperConstructor;
import alix.common.utils.netty.WrapperTransformer;
import io.netty.buffer.ByteBuf;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.PacketOutCommands;

import java.util.List;

abstract class AbstractLimboCommand implements LimboCommand {

    private static final boolean supportAllChars = true;

    private final ByteBuf encoded;
    private final PacketSnapshot snapshot;

    AbstractLimboCommand(ByteBuf encoded) {
        this.encoded = encoded;
        this.snapshot = PacketSnapshot.of(new PacketOutCommands(this));
    }

    @Override
    public ByteBuf getEncodedNoId() {
        return this.encoded;
    }

    @Override
    public PacketSnapshot getPacketSnapshot() {
        return this.snapshot;
    }

    static AbstractLimboCommand construct0(List<String> aliases, String arg1Name) {
        return new OneArg(aliases, arg1Name);
    }

    static AbstractLimboCommand construct0(List<String> aliases, String arg1Name, String arg2Name) {
        return new TwoArg(aliases, arg1Name, arg2Name);
    }

    private static final class OneArg extends AbstractLimboCommand {

        private OneArg(List<String> aliases, String arg1Name) {
            super(CommandsWrapperConstructor.constructOneArg(aliases, arg1Name, supportAllChars, WrapperTransformer.DYNAMIC_NO_ID));
        }
    }

    private static final class TwoArg extends AbstractLimboCommand {

        private TwoArg(List<String> aliases, String arg1Name, String arg2Name) {
            super(CommandsWrapperConstructor.constructTwoArg(aliases, arg1Name, arg2Name, supportAllChars, WrapperTransformer.DYNAMIC_NO_ID));
        }
    }
}