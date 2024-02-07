package shadow.utils.holders.packet.constructors;

import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import org.bukkit.GameMode;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.access.ProtocolAccess;

import java.lang.reflect.Constructor;

public final class OutGameStatePacketConstructor {

    public static final Object ADVENTURE_GAMEMODE_PACKET;

    static {
        Class<?> packetClazz = ReflectionUtils.outGameStatePacketClass;
        Object adventurePacket = null;
        try {
            for (Constructor<?> c : packetClazz.getConstructors()) {
                Class<?>[] params = c.getParameterTypes();
                if (params.length == 2) {
                    if (params[0] == int.class && params[1] == float.class) {
                        adventurePacket = c.newInstance(3, 2f);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        if (adventurePacket == null && ReflectionUtils.protocolVersion)
            adventurePacket = ProtocolAccess.newGameModePacket(GameMode.ADVENTURE);
        if (adventurePacket == null) throw new AlixError();
        ADVENTURE_GAMEMODE_PACKET = adventurePacket;
    }

    private OutGameStatePacketConstructor() {
    }
}