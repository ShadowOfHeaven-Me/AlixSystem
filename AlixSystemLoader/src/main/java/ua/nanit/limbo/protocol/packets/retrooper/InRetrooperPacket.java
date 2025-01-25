package ua.nanit.limbo.protocol.packets.retrooper;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ua.nanit.limbo.protocol.PacketIn;

public abstract class InRetrooperPacket<T extends PacketWrapper> extends RetrooperPacket<T> implements PacketIn {

    protected InRetrooperPacket(T wrapper) {
        super(wrapper);
    }

    protected InRetrooperPacket(Class<T> wrapperClazz) {
        super(wrapperClazz);
    }
}