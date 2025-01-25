package alix.common.utils.collections.list;

public class SpecializedList<T> {

    private Object[] data;
    private int size;

    public SpecializedList() {
        this(0);
    }

    public SpecializedList(int size) {
        this.data = new Object[size];
    }

    public void add(T t) {
        Object[] array = new Object[size + 1];
        System.arraycopy(data, 0, array, 0, size);
        this.data = array;
        data[size++] = t;
    }

    public void remove(T t) {
        for (int i = 0; i < size; i++) {
            if(t.equals(data[i])) {

            }
        }
    }

    @SuppressWarnings({"unchecked", "SuspiciousArrayCast"})
    public T[] toArray() {
        return size != 0 ? (T[]) data : null;
    }

    @SuppressWarnings("unchecked")
    public T last() {
        if (size == 0) return null;
        return (T) data[size - 1];
    }
}