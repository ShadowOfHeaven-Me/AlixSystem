package ua.nanit.limbo.protocol.packets.factory;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.HandleMask;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.packets.factory.recycler.PacketRecycler;

import java.util.function.Supplier;

public final class PacketFactory {

    private static final boolean __ENABLE_CACHING = false;
    private static final boolean USE_CACHE = __ENABLE_CACHING && PacketRecycler.isAvailable();

    private final Supplier<? extends Packet> supplier;
    private final Packet cached;//empty, used only for isSkippable
    private final PacketCache cache;
    private final boolean skippable;

    public PacketFactory(Supplier<? extends Packet> supplier) {
        this.supplier = supplier;

        var packet = supplier.get();
        this.skippable = HandleMask.isSkippable0(packet.getClass());
        //let the packet be gc'ed if not needed
        this.cached = this.skippable ? null : packet;
        this.cache = USE_CACHE && PacketCache.hasCacheEnabled(packet) ? new PacketCache(this) : null;
    }

    public boolean isPacketSkippable(ClientConnection connection) {
        return this.skippable || this.cached.isSkippable(connection);
    }

    public Packet newPacket() {
        return this.cache != null ? this.cache.cached() : this.newPacket0();
    }

    Packet newPacket0() {
        return this.supplier.get();
    }
}