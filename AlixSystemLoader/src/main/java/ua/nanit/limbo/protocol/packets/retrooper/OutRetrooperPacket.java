package ua.nanit.limbo.protocol.packets.retrooper;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ua.nanit.limbo.protocol.PacketOut;

public abstract class OutRetrooperPacket<T extends PacketWrapper> extends RetrooperPacket<T> implements PacketOut {

    protected OutRetrooperPacket(T wrapper) {
        super(wrapper);
    }

    protected OutRetrooperPacket(Class<T> wrapperClazz) {
        super(wrapperClazz);
    }
}