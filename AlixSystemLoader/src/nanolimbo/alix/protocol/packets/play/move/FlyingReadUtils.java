package nanolimbo.alix.protocol.packets.play.move;

import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.throwable.AlixException;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

final class FlyingReadUtils {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    private static final long positionChangedOffset, rotationChangedOffset;

    static {
        positionChangedOffset = UNSAFE.objectFieldOffset(getField("positionChanged"));
        rotationChangedOffset = UNSAFE.objectFieldOffset(getField("rotationChanged"));
    }

    static void setPositionChanged(WrapperPlayClientPlayerFlying wrapper) {
        UNSAFE.putBoolean(wrapper, positionChangedOffset, true);
    }

    static void setRotationChanged(WrapperPlayClientPlayerFlying wrapper) {
        UNSAFE.putBoolean(wrapper, rotationChangedOffset, true);
    }

    private static Field getField(String name) {
        try {
            return WrapperPlayClientPlayerFlying.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new AlixException(e);
        }
    }
}