package ua.nanit.limbo.protocol.packets.retrooper;

import alix.common.utils.netty.WrapperUtils;
import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.Version;

public abstract class InRetrooperPacket<T extends PacketWrapper> extends RetrooperPacket<T> implements PacketIn {

    //private final Class<T> wrapperClazz;
    private final T wrapper;

    protected InRetrooperPacket(Class<T> wrapperClazz) {
        this.wrapper = WrapperUtils.allocEmpty(wrapperClazz);
    }

    @Override
    public final void decode(ByteMessage msg, Version version) {
        WrapperUtils.readEmptyWrapperNoID(msg.getBuf(), version.getRetrooperVersion(), this.wrapper);
    }

    @Override
    public final T wrapper() {
        return wrapper;
    }

    /*@Override
    public String toString() {
        return this.wrapperClazz.getSimpleName();
    }*/
}