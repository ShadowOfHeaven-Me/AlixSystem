package ua.nanit.limbo.util.map;

public final class VersionMap<T> extends AbstractVersionMap<T> {

    //An alternative to an EnumMap
    public VersionMap() {
    }

    @Override
    void setElement(Object[] data, int i, T val) {
        data[i] = val;
    }

    @Override
    T getElement(Object[] data, int i) {
        return (T) data[i];
    }
}