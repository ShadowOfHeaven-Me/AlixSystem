package alix.common.utils.other;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

//Thanks geol ^^
public final class AtomicFloat {

    private static final VarHandle VALUE;
    private volatile float value;

    public AtomicFloat() {
    }

    public AtomicFloat(float initVal) {
        this.value = initVal;
    }

    public float get() {
        return value;
    }

    public float addAndGet(float v) {
        VALUE.getAndAdd(this, v);
        return value;
    }

    public float getAndAdd(float v) {
        return (float) VALUE.getAndAdd(this, v);
    }

    public AtomicFloat incrementAndGetSelf() {
        addAndGet(1);
        return this;
    }

    static {
        try {
            VALUE = MethodHandles.lookup().findVarHandle(AtomicFloat.class, "value", float.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to obtain varhandle of object", e);
        }
    }
}