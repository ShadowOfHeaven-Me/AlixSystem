package alix.common.packets.inventory;

import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.util.function.ToIntFunction;

import static com.github.retrooper.packetevents.protocol.player.ClientVersion.V_1_20_3;

public enum AlixInventoryType {

    GENERIC_9X1(0),
    GENERIC_9X2(1),
    GENERIC_9X3(2),
    GENERIC_9X4(3),
    GENERIC_9X5(4),
    GENERIC_9X6(5),
    GENERIC_3X3(6),
    CRAFTER_3X3(7),//added in 1.20.3
    ANVIL(ver -> ver.isNewerThan(V_1_20_3) ? 8 : 7);

    private final ToIntFunction<ClientVersion> func;

    AlixInventoryType(ToIntFunction<ClientVersion> func) {
        this.func = func;
    }

    AlixInventoryType(int id) {
        this.func = v -> id;
    }

    public int getId(ClientVersion version) {
        return this.func.applyAsInt(version);
    }

    public int size() {
        return size(this);
    }

    public static AlixInventoryType generic9xN(int size) {
        if (size % 9 != 0 || size <= 0 || size > 54) throw new AlixException("Invalid inventory size " + size + "!");
        int N = size / 9;
        return values()[N - 1];
    }

    public static int size(AlixInventoryType type) {
        int ord = type.ordinal();
        if (ord <= GENERIC_9X6.ordinal()) return (ord + 1) * 9;
        switch (type) {
            case GENERIC_3X3:
            case CRAFTER_3X3:
                return 9;
            case ANVIL:
                return 3;
            default:
                throw new AlixException("Invalid: " + type + "!");
        }
    }
}