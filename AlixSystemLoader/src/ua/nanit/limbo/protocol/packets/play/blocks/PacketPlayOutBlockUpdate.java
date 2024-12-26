package ua.nanit.limbo.protocol.packets.play.blocks;

import alix.common.utils.other.throwable.AlixError;
import alix.libs.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import alix.libs.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import alix.libs.com.github.retrooper.packetevents.util.Vector3i;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketPlayOutBlockUpdate extends OutRetrooperPacket<WrapperPlayServerBlockChange> {

    private WrappedBlockState state;

    public PacketPlayOutBlockUpdate() {
        super(WrapperPlayServerBlockChange.class);
    }

    public PacketPlayOutBlockUpdate setType(Version version, StateType state) {
        ClientVersion clientVersion = version.getRetrooperVersion().toClientVersion();

        this.state = WrappedBlockState.getDefaultState(clientVersion, state);
        return this;
    }

    public WrappedBlockState getState() {
        return state;
    }

    public PacketPlayOutBlockUpdate setPosition(Vector3i pos) {
        super.wrapper().setBlockPosition(pos);
        return this;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        super.wrapper().setBlockState(this.state);
        super.encode(msg, version);
    }

    @Override
    public WrapperPlayServerBlockChange wrapper() {
        throw new AlixError("wrapper() invoked on PacketPlayOutBlockUpdate");
    }
}