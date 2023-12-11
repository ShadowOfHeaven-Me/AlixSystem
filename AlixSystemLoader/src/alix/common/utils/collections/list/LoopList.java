package alix.common.utils.collections.list;

public final class LoopList<T> {

    private final T[] values;
    private final short maxIndex;
    private short currentIndex;

    public LoopList(T[] values) {
        this.values = values;
        int l = values.length;
        if (l > 32767)
            throw new RuntimeException("Invalid array size " + l + " with the max of 32767");
        this.maxIndex = (short) l;
    }

    public void setNext(T value) {
        this.values[this.nextIndex()] = value;
    }

    public void setValue(int i, T value) {
        values[i] = value;
    }

    public T next() {
        return this.values[this.nextIndex()];
    }

    private int nextIndex() {
        return currentIndex != maxIndex ? currentIndex++ : (currentIndex = 0);
    }

    public final boolean contains(T v) {
        for (T t : values) if (v.equals(t)) return true;
        return false;
    }

    public T get(short i) {
        return this.values[i];
    }

    public T[] getValues() {
        return values;
    }

    public short size() {
        return maxIndex;
    }

    public short getCurrentIndex() {
        return currentIndex;
    }
}