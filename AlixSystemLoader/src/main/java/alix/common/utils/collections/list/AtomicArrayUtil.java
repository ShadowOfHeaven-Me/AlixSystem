package alix.common.utils.collections.list;

import alix.common.utils.other.throwable.AlixException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class AtomicArrayUtil {

    //Ensure the array's elements thread visibility by using MethodHandles
    private static final MethodHandle setElement = MethodHandles.arrayElementSetter(Object[].class);
    private static final MethodHandle getElement = MethodHandles.arrayElementGetter(Object[].class);

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
}