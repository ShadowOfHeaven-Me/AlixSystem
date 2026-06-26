package alix.common.utils.other;

import alix.common.reflection.CommonReflection;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class AlixUnsafe {

    private static final Unsafe UNSAFE = tryGetUnsafe0();
    private static final boolean hasUnsafe = UNSAFE != null;

    private static Unsafe tryGetUnsafe0() {
        try {
            return (Unsafe) CommonReflection.getFieldResult(Unsafe.class, "theUnsafe", null);
        } catch (Throwable e) {
            return null;
        }
    }

    public static <T> T alloc(Class<T> clazz) {
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new AlixException(e);
        }
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    public static long objectFieldOffset(Class<?> clazz, String fieldName) {
        if (!hasUnsafe) throw new AlixError("No Unsafe!");
        return UNSAFE.objectFieldOffset(CommonReflection.getDeclaredField(clazz, fieldName));
    }

    public static boolean hasUnsafe() {
        return hasUnsafe;
    }

    public static Object getFieldValue(Field f, Object obj) {
        long offset;
        Object base;
        if (Modifier.isStatic(f.getModifiers())) {
            offset = UNSAFE.staticFieldOffset(f);
            base = UNSAFE.staticFieldBase(f);
            if (obj != null)
                throw new AlixException("Object non-null for static field " + f.getName() + "!");
        } else {
            offset = UNSAFE.objectFieldOffset(f);
            base = obj;
            if (obj == null)
                throw new AlixException("Object null for object field " + f.getName() + "!");
        }

        Class<?> type = f.getType();
        return Modifier.isVolatile(f.getModifiers()) ? getValueVolatile(base, offset, type) : getValue(base, offset, type);
    }

    public static void setFieldValue(Field f, Object obj, Object value) {
        long offset;
        Object base;
        if (Modifier.isStatic(f.getModifiers())) {
            offset = UNSAFE.staticFieldOffset(f);
            base = UNSAFE.staticFieldBase(f);
            if (obj != null)
                throw new AlixException("Object non-null for static field " + f.getName() + "!");
        } else {
            offset = UNSAFE.objectFieldOffset(f);
            base = obj;
            if (obj == null)
                throw new AlixException("Object null for object field " + f.getName() + "!");
        }
        Class<?> type = f.getType();
        if (Modifier.isVolatile(f.getModifiers())) putValueVolatile(base, offset, value, type);
        else putValue(base, offset, value, type);
    }

    public static void putValue(Object base, long offset, Object value, Class<?> c) {
        if (c == byte.class) UNSAFE.putByte(base, offset, (byte) value);
        else if (c == short.class) UNSAFE.putShort(base, offset, (short) value);
        else if (c == int.class) UNSAFE.putInt(base, offset, (int) value);
        else if (c == long.class) UNSAFE.putLong(base, offset, (long) value);
        else if (c == float.class) UNSAFE.putFloat(base, offset, (float) value);
        else if (c == double.class) UNSAFE.putDouble(base, offset, (double) value);
        else if (c == char.class) UNSAFE.putChar(base, offset, (char) value);
        else if (c == boolean.class) UNSAFE.putBoolean(base, offset, (boolean) value);
        else UNSAFE.putObject(base, offset, value);
    }

    public static void putValueVolatile(Object base, long offset, Object value, Class<?> c) {
        if (c == byte.class) UNSAFE.putByteVolatile(base, offset, (byte) value);
        else if (c == short.class) UNSAFE.putShortVolatile(base, offset, (short) value);
        else if (c == int.class) UNSAFE.putIntVolatile(base, offset, (int) value);
        else if (c == long.class) UNSAFE.putLongVolatile(base, offset, (long) value);
        else if (c == float.class) UNSAFE.putFloatVolatile(base, offset, (float) value);
        else if (c == double.class) UNSAFE.putDoubleVolatile(base, offset, (double) value);
        else if (c == char.class) UNSAFE.putCharVolatile(base, offset, (char) value);
        else if (c == boolean.class) UNSAFE.putBooleanVolatile(base, offset, (boolean) value);
        else UNSAFE.putObjectVolatile(base, offset, value);
    }

    public static Object getValue(Object base, long offset, Class<?> c) {
        if (c == byte.class) return UNSAFE.getByte(base, offset);
        else if (c == short.class) return UNSAFE.getShort(base, offset);
        else if (c == int.class) return UNSAFE.getInt(base, offset);
        else if (c == long.class) return UNSAFE.getLong(base, offset);
        else if (c == float.class) return UNSAFE.getFloat(base, offset);
        else if (c == double.class) return UNSAFE.getDouble(base, offset);
        else if (c == char.class) return UNSAFE.getChar(base, offset);
        else if (c == boolean.class) return UNSAFE.getBoolean(base, offset);
        else return UNSAFE.getObject(base, offset);
    }

    public static Object getValueVolatile(Object base, long offset, Class<?> c) {
        if (c == byte.class) return UNSAFE.getByteVolatile(base, offset);
        else if (c == short.class) return UNSAFE.getShortVolatile(base, offset);
        else if (c == int.class) return UNSAFE.getIntVolatile(base, offset);
        else if (c == long.class) return UNSAFE.getLongVolatile(base, offset);
        else if (c == float.class) return UNSAFE.getFloatVolatile(base, offset);
        else if (c == double.class) return UNSAFE.getDoubleVolatile(base, offset);
        else if (c == char.class) return UNSAFE.getCharVolatile(base, offset);
        else if (c == boolean.class) return UNSAFE.getBooleanVolatile(base, offset);
        else return UNSAFE.getObjectVolatile(base, offset);
    }

    private AlixUnsafe() {
    }
}