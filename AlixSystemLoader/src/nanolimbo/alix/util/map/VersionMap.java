package nanolimbo.alix.util.map;

public final class VersionMap<T> extends AbstractVersionMap<T> {

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