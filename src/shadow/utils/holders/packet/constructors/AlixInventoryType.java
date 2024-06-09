package shadow.utils.holders.packet.constructors;

import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public enum AlixInventoryType {

    GENERIC_9X1(0),
    GENERIC_9X2(1),
    GENERIC_9X3(2),
    GENERIC_9X4(3),
    GENERIC_9X5(4),
    GENERIC_9X6(5),
    GENERIC_3X3(6),
    CRAFTER_3X3(7),//added in 1.20.3
    ANVIL(8);

    private final int id;

    AlixInventoryType(int id) {
        ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
        if (version.isOlderThan(ServerVersion.V_1_20_3)) this.id = id > 7 ? id - 1 : id;
        else this.id = id;
    }

    private static boolean isBetween(ServerVersion from, ServerVersion to) {
        ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
        return version.isNewerThanOrEquals(from) && version.isOlderThanOrEquals(to);
    }

    public int getId() {
        return id;
    }

    public static AlixInventoryType generic9xN(int size) {
        if (size % 9 != 0 || size <= 0 || size > 54) throw new AlixException("Invalid inventory size " + size + "!");
        int N = size / 9;
        return values()[N - 1];
    }
}