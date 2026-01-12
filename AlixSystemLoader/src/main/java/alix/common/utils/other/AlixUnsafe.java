package alix.common.utils.other;

import alix.common.reflection.CommonReflection;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class AlixUnsafe {

    private static final Unsafe unsafe = tryGetUnsafe0();
    private static final boolean hasUnsafe = unsafe != null;

    private static Unsafe tryGetUnsafe0() {
        try {
            return (Unsafe) CommonReflection.getFieldResult(Unsafe.class, "theUnsafe", null);
        } catch (Throwable e) {
            return null;
        }
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }

    public static long objectFieldOffset(Class<?> clazz, String fieldName) {
        if (!hasUnsafe) throw new AlixError("No Unsafe!");
        return unsafe.objectFieldOffset(CommonReflection.getDeclaredField(clazz, fieldName));
    }

    public static boolean hasUnsafe() {
        return hasUnsafe;
    }

    public static Object getFieldValue(Field f, Object obj) {
        long offset;
        Object base;
        if (Modifier.isStatic(f.getModifiers())) {
            offset = unsafe.staticFieldOffset(f);
            base = unsafe.staticFieldBase(f);
            if (obj != null)
                throw new AlixException("Object non-null for static field " + f.getName() + "!");
        } else {
            offset = unsafe.objectFieldOffset(f);
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
            offset = unsafe.staticFieldOffset(f);
            base = unsafe.staticFieldBase(f);
            if (obj != null)
                throw new AlixException("Object non-null for static field " + f.getName() + "!");
        } else {
            offset = unsafe.objectFieldOffset(f);
            base = obj;
            if (obj == null)
                throw new AlixException("Object null for object field " + f.getName() + "!");
        }
        Class<?> type = f.getType();
        if (Modifier.isVolatile(f.getModifiers())) putValueVolatile(base, offset, value, type);
        else putValue(base, offset, value, type);
    }

    public static void putValue(Object base, long offset, Object value, Class<?> c) {
        if (c == byte.class) unsafe.putByte(base, offset, (byte) value);
        else if (c == short.class) unsafe.putShort(base, offset, (short) value);
        else if (c == int.class) unsafe.putInt(base, offset, (int) value);
        else if (c == long.class) unsafe.putLong(base, offset, (long) value);
        else if (c == float.class) unsafe.putFloat(base, offset, (float) value);
        else if (c == double.class) unsafe.putDouble(base, offset, (double) value);
        else if (c == char.class) unsafe.putChar(base, offset, (char) value);
        else if (c == boolean.class) unsafe.putBoolean(base, offset, (boolean) value);
        else unsafe.putObject(base, offset, value);
    }

    public static void putValueVolatile(Object base, long offset, Object value, Class<?> c) {
        if (c == byte.class) unsafe.putByteVolatile(base, offset, (byte) value);
        else if (c == short.class) unsafe.putShortVolatile(base, offset, (short) value);
        else if (c == int.class) unsafe.putIntVolatile(base, offset, (int) value);
        else if (c == long.class) unsafe.putLongVolatile(base, offset, (long) value);
        else if (c == float.class) unsafe.putFloatVolatile(base, offset, (float) value);
        else if (c == double.class) unsafe.putDoubleVolatile(base, offset, (double) value);
        else if (c == char.class) unsafe.putCharVolatile(base, offset, (char) value);
        else if (c == boolean.class) unsafe.putBooleanVolatile(base, offset, (boolean) value);
        else unsafe.putObjectVolatile(base, offset, value);
    }

    public static Object getValue(Object base, long offset, Class<?> c) {
        if (c == byte.class) return unsafe.getByte(base, offset);
        else if (c == short.class) return unsafe.getShort(base, offset);
        else if (c == int.class) return unsafe.getInt(base, offset);
        else if (c == long.class) return unsafe.getLong(base, offset);
        else if (c == float.class) return unsafe.getFloat(base, offset);
        else if (c == double.class) return unsafe.getDouble(base, offset);
        else if (c == char.class) return unsafe.getChar(base, offset);
        else if (c == boolean.class) return unsafe.getBoolean(base, offset);
        else return unsafe.getObject(base, offset);
    }

    public static Object getValueVolatile(Object base, long offset, Class<?> c) {
        if (c == byte.class) return unsafe.getByteVolatile(base, offset);
        else if (c == short.class) return unsafe.getShortVolatile(base, offset);
        else if (c == int.class) return unsafe.getIntVolatile(base, offset);
        else if (c == long.class) return unsafe.getLongVolatile(base, offset);
        else if (c == float.class) return unsafe.getFloatVolatile(base, offset);
        else if (c == double.class) return unsafe.getDoubleVolatile(base, offset);
        else if (c == char.class) return unsafe.getCharVolatile(base, offset);
        else if (c == boolean.class) return unsafe.getBooleanVolatile(base, offset);
        else return unsafe.getObjectVolatile(base, offset);
    }

    private AlixUnsafe() {
    }
}