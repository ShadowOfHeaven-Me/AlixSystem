package shadow.utils.main;

import io.netty.util.internal.PlatformDependent;
import shadow.utils.holders.ReflectionUtils;
import sun.misc.Unsafe;

public final class AlixUnsafe {

    private static final boolean hasUnsafe = PlatformDependent.hasUnsafe();
    private static final Unsafe unsafe = hasUnsafe ? (Unsafe) ReflectionUtils.getFieldResult(Unsafe.class, "theUnsafe", null) : null;

    public static long fieldOffset(Class<?> clazz, String name) {
        return hasUnsafe ? unsafe.objectFieldOffset(ReflectionUtils.getDeclaredField(clazz, name)) : -1;
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