package ua.nanit.limbo.commands;

import alix.common.packets.command.CommandsWrapperConstructor;
import alix.common.utils.netty.WrapperTransformer;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.netty.buffer.ByteBuf;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.packets.play.PacketOutCommands;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

import java.util.List;

final class LimboCommandImpl implements LimboCommand {

    //private final ByteBuf encoded;
    private final PacketSnapshot snapshot;
    private final List<String> aliases;
    private final String[] argNames;

    LimboCommandImpl(List<String> aliases, String... argNames) {
        this.aliases = aliases;
        this.argNames = argNames;
        //this.encoded = encoded;
        this.snapshot = PacketSnapshot.of(new PacketOutCommands(this));
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        ServerVersion ver = version.getRetrooperVersion();
        ByteBuf encodedNoId = switch (argNames.length) {
            case 1 -> CommandsWrapperConstructor.constructOneArg(aliases, argNames[0], WrapperTransformer.DYNAMIC_NO_ID, ver);
            case 2 -> CommandsWrapperConstructor.constructTwoArg(aliases, argNames[0], argNames[1], WrapperTransformer.DYNAMIC_NO_ID, ver);
            default -> CommandsWrapperConstructor.constructArgs(aliases, List.of(argNames), WrapperTransformer.DYNAMIC_NO_ID, ver);
        };

        msg.writeBytes(encodedNoId, 0, encodedNoId.readableBytes());
        encodedNoId.release();
    }

    @Override
    public PacketSnapshot getPacketSnapshot() {
        return this.snapshot;
    }

    static LimboCommandImpl construct0(List<String> aliases, String arg1Name) {
        //Log.error("arg1Name= '" + arg1Name + "'");
        return new LimboCommandImpl(aliases, arg1Name);
    }

    static LimboCommandImpl construct0(List<String> aliases, String arg1Name, String arg2Name) {
        return new LimboCommandImpl(aliases, arg1Name, arg2Name);
    }

    static LimboCommandImpl construct0(List<String> aliases, String arg1Name, String arg2Name, String arg3Name) {
        return new LimboCommandImpl(aliases, arg1Name, arg2Name, arg3Name);
    }

    /*private static final class OneArg extends AbstractLimboCommand {

        private OneArg(List<String> aliases, String arg1Name) {
            super(CommandsWrapperConstructor.constructOneArg(aliases, arg1Name, supportAllChars, WrapperTransformer.DYNAMIC_NO_ID));
        }
    }

    private static final class TwoArg extends AbstractLimboCommand {

        private TwoArg(List<String> aliases, String arg1Name, String arg2Name) {
            super(CommandsWrapperConstructor.constructTwoArg(aliases, arg1Name, arg2Name, supportAllChars, WrapperTransformer.DYNAMIC_NO_ID));
        }
    }*/
}