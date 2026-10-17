package ua.nanit.limbo.protocol.packets.factory;

import alix.common.utils.other.throwable.AlixError;
import ua.nanit.limbo.protocol.Packet;

public final class PacketCache {

    private final ThreadLocal<CachedProfile> pool;

    PacketCache(PacketFactory factory) {
        this.pool = ThreadLocal.withInitial(() -> new CachedProfile(factory.newPacket0()));
    }

    Packet cached() {
        return this.pool.get().borrow();
    }

    private static final class CachedProfile {
        private final Packet packet;
        boolean borrowed;//cheap but faulty

        CachedProfile(Packet packet) {
            this.packet = packet;
        }

        Packet borrow() {
            if (this.borrowed)
                throw new AlixError("Cannot again borrow packet " + packet.getClass().getSimpleName());

            this.borrowed = true;
            return this.packet;
        }
    }

    public static void recycle(Packet packet) {
        if (!hasCacheEnabled(packet)) return;


    }

    public static boolean hasCacheEnabled(Packet packet) {
        return !(packet instanceof NoPacketCache);
    }
}