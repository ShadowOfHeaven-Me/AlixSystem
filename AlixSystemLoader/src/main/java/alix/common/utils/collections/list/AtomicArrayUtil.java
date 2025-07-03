package alix.common.utils.collections.list;

import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.throwable.AlixException;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public final class AtomicArrayUtil {

    //Ensure the array's elements thread visibility by using MethodHandles
    private static final MethodHandle setElement = MethodHandles.arrayElementSetter(Object[].class);
    private static final MethodHandle getElement = MethodHandles.arrayElementGetter(Object[].class);

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    private static final VarHandle varHandleInt = MethodHandles.arrayElementVarHandle(int[].class);

    public static void setElement(Object[] a, int i, Object o) {
        try {
            setElement.invoke(a, i, o);
        } catch (Throwable e) {
            throw new AlixException(e);
        }
    }

    public static Object getElement(Object[] a, int i) {
        try {
            return getElement.invoke(a, i);
        } catch (Throwable e) {
            throw new AlixException(e);
        }
    }

    public static void setElement(int[] a, int i, int o) {
        try {
            //UNSAFE.putIntVolatile(a, );
            varHandleInt.setVolatile(a, i, o);
            //setElementInt.invokeExact(a, i, o);
        } catch (Throwable e) {
            throw new AlixException(e);
        }
    }

    public static int getElement(int[] a, int i) {
        try {
            return (int) varHandleInt.getVolatile(a, i);
        } catch (Throwable e) {
            throw new AlixException(e);
        }
    }
}