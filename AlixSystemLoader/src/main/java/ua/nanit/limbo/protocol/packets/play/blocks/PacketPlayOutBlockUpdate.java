package ua.nanit.limbo.protocol.packets.play.blocks;

import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.function.Consumer;

public final class PacketPlayOutBlockUpdate extends OutRetrooperPacket<WrapperPlayServerBlockChange> {

    private Consumer<WrappedBlockState> transformer = AlixCommonUtils.EMPTY_CONSUMER;
    private StateType stateType;

    public PacketPlayOutBlockUpdate() {
        super(WrapperPlayServerBlockChange.class);
    }

    public PacketPlayOutBlockUpdate setType(StateType stateType) {
        this.stateType = stateType;
        return this;
    }

    public PacketPlayOutBlockUpdate setTransformer(Consumer<WrappedBlockState> transformer) {
        this.transformer = transformer;
        return this;
    }

    public PacketPlayOutBlockUpdate setPosition(Vector3i pos) {
        super.wrapper().setBlockPosition(pos);
        return this;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        ClientVersion clientVersion = version.getRetrooperVersion().toClientVersion();
        var state = WrappedBlockState.getDefaultState(clientVersion, this.stateType);

        this.transformer.accept(state);

        super.wrapper().setBlockState(state);

        //Log.error("ENCODED TYPE ID " + state.getGlobalId() + " VERSION " + version + " CLIENT VER: " + clientVersion + " POS: " + super.wrapper().getBlockPosition());
        super.encode(msg, version);
    }

    @Override
    public WrapperPlayServerBlockChange wrapper() {
        throw new AlixError("wrapper() invoked on PacketPlayOutBlockUpdate");
    }
}