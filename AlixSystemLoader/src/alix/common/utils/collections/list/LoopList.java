package alix.common.utils.collections.list;

public class LoopList<T> {

    private final T[] values;
    private final short maxIndex;
    private short currentIndex;

    public LoopList(T[] values) {
        this.values = values;
        int l = values.length;
        if (l > 32767 || l < 0)
            throw new RuntimeException("Invalid array size " + l + " with the max of 32767 and min of 0!");
        this.maxIndex = (short) l;
    }

    public void setNext(T value) {
        values[currentIndex++] = value;
        if (currentIndex == maxIndex) currentIndex = 0;
    }

    public void setValue(int i, T value) {
        values[i] = value;
    }

    public T next() {
        if (currentIndex == maxIndex) currentIndex = 0;
        return values[currentIndex++];
    }

    public boolean contains(T v) {
        for (T t : values) {
            if (t == null) break;
            if (v.equals(t)) return true;
        }
        return false;
    }

    public T get(byte i) {
        return values[i];
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