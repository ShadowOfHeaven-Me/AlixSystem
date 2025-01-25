package alix.common.utils.other;

@SuppressWarnings("NonAtomicOperationOnVolatileField")
public final class VolatileInt {

    private volatile int value;

    public VolatileInt() {
    }

    public VolatileInt(int value) {
        this.value = value;
    }

    public VolatileInt add(int d) {
        this.value += d;
        return this;
    }

    public int getValue() {
        return value;
    }
}