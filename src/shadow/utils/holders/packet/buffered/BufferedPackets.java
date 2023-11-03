package shadow.utils.holders.packet.buffered;

import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;

public class BufferedPackets {

    private static final Object[] anvilAllItemsPackets = new Object[128];
    private static final Object[] anvilFirstTwoItemsPackets = new Object[128];

    public static Object getAllItemsPacketOf(int id) {
        return id < 129 ? anvilAllItemsPackets[id - 1] : null;
    }

    public static Object getInvalidIndicatePacketOf(int id) {
        return id < 129 ? anvilFirstTwoItemsPackets[id - 1] : null;
    }

    public static void init(Object ALL_ITEMS_LIST, Object INVALID_INDICATE_ITEMS_LIST) {
        for (int i = 0; i < 128; i++) {
            anvilAllItemsPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, ALL_ITEMS_LIST);
            anvilFirstTwoItemsPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, INVALID_INDICATE_ITEMS_LIST);
        }
    }
}