package ua.nanit.limbo.protocol.packets.retrooper;

import alix.common.utils.netty.WrapperUtils;
import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.registry.Version;

public abstract class OutRetrooperPacket<T extends PacketWrapper> extends RetrooperPacket<T> implements PacketOut {

    private final T wrapper;

    protected OutRetrooperPacket(T wrapper) {
        this.wrapper = wrapper;
    }

    protected OutRetrooperPacket(Class<T> wrapperClazz) {
        this.wrapper = WrapperUtils.allocEmpty(wrapperClazz);
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        WrapperUtils.writeNoID(this.wrapper, msg.getBuf(), version.getRetrooperVersion());
    }

    @Override
    public T wrapper() {
        return wrapper;
    }

    /*@Override
    public String toString() {
        return this.wrapper.getClass().getSimpleName();
    }*/
}