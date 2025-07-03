package ua.nanit.limbo.protocol;

import lombok.SneakyThrows;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.Packet.Skip;
import ua.nanit.limbo.server.LimboServer;

public final class HandleMask {

    /*private static final Map<Class<? extends Packet>, Boolean> SKIPPABLE = new IdentityHashMap<>(1 << 5);

    public static void register(Class<? extends Packet> clazz) {
        boolean skippable = isSkippable0(clazz);
        SKIPPABLE.put(clazz, skippable);
        //if (skippable) Log.error("SKIPPABLE: " + clazz + " TOTAL: " + SKIPPABLE.size());
    }

    //An idea taken from Netty's @Skip annotation optimization
    public static boolean isSkippable(Class<? extends Packet> clazz) {
        return SKIPPABLE.get(clazz);
    }*/

    @SneakyThrows
    public static boolean isSkippable0(Class<? extends Packet> clazz) {
        return clazz.getMethod("handle", ClientConnection.class, LimboServer.class).isAnnotationPresent(Skip.class);
    }
}