package shadow.utils.main;

import alix.common.utils.other.throwable.AlixError;
import io.netty.util.internal.PlatformDependent;
import shadow.utils.holders.ReflectionUtils;
import sun.misc.Unsafe;

public final class AlixUnsafe {

    private static final boolean hasUnsafe = PlatformDependent.hasUnsafe();
    private static final Unsafe unsafe = hasUnsafe ? (Unsafe) ReflectionUtils.getFieldResult(Unsafe.class, "theUnsafe", null) : null;

    public static long objectFieldOffset(Class<?> clazz, String fieldName) {
        if (!hasUnsafe) throw new AlixError("No Unsafe!");
        return unsafe.objectFieldOffset(ReflectionUtils.getDeclaredField(clazz, fieldName));
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }

    public static boolean hasUnsafe() {
        return hasUnsafe;
    }

    private AlixUnsafe() {
    }
}