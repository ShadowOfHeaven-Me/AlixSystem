package alix.common.reflection;

import alix.common.utils.other.throwable.AlixException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class CommonReflection {

    public static Method getMethod(Class<?> clazz, String name, Class<?>... params) {
        try {
            Method method = clazz.getMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            throw new AlixException(e, "No method: " + clazz.getSimpleName() + "." + name + "(" + Arrays.toString(params) + ")");
        }
    }

    public static Field getFieldAccessibleByType(Class<?> instClass, Class<?> typeClass) {
        for (Field field : instClass.getDeclaredFields())
            if (typeClass.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field;
            }
        throw new ExceptionInInitializerError("No valid field with " + typeClass + " in " + instClass);
    }

    public static <T> T invoke(Method method, Object obj, Object... args) {
        try {
            return (T) method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object get(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void set(Field field, Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getFieldResult(Class<?> clazz, String name, Object obj) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);//AlixException
        }
    }

    public static Field getDeclaredFieldAccessible(Class<?> clazz, String name) {
        try {
            var field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new AlixException(e);
        }
    }

    public static Field getDeclaredField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new AlixException(e);
        }
    }

    public static Field getFieldOrNullIfAbsent(Object obj, String name) {
        try {
            return obj.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Class<?> forName(String... names) {
        for (String s : names) {
            try {
                return Class.forName(s);
            } catch (ClassNotFoundException ignored) {

            }
        }
        throw new ExceptionInInitializerError("Not found: " + Arrays.toString(names));
    }
}