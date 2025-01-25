package ua.nanit.limbo.protocol.packets.retrooper;

import alix.common.utils.netty.WrapperUtils;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.Version;

abstract class RetrooperPacket<T extends PacketWrapper> implements Packet {

    private final T wrapper;

    RetrooperPacket(T wrapper) {
        this.wrapper = wrapper;
    }

    RetrooperPacket(Class<T> wrapperClazz) {
        this.wrapper = WrapperUtils.allocEmpty(wrapperClazz);
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        WrapperUtils.writeNoID(this.wrapper, msg.getBuf(), version.getRetrooperVersion());
    }

    @Override
    public final void decode(ByteMessage msg, Version version) {
        WrapperUtils.readEmptyWrapperNoID(msg.getBuf(), version.getRetrooperVersion(), this.wrapper);
    }

    public T wrapper() {
        return wrapper;
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName();
    }
}