package ua.nanit.limbo.commands;

import alix.common.packets.command.CommandsWrapperConstructor;
import alix.common.utils.netty.WrapperTransformer;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.netty.buffer.ByteBuf;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.PacketOutCommands;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.List;

import static alix.common.utils.config.ConfigProvider.config;

final class LimboCommandImpl implements LimboCommand {

    private static final boolean supportAllChars = config.getBoolean("command-support-all-characters");

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
        ByteBuf encodedNoId;

        if (argNames.length == 1)
            encodedNoId = CommandsWrapperConstructor.constructOneArg(aliases, argNames[0], supportAllChars, WrapperTransformer.DYNAMIC_NO_ID, ver);
        else
            encodedNoId = CommandsWrapperConstructor.constructTwoArg(aliases, argNames[0], argNames[1], supportAllChars, WrapperTransformer.DYNAMIC_NO_ID, ver);

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