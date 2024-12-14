package nanolimbo.alix.util.map;

import nanolimbo.alix.protocol.registry.Version;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class AbstractVersionMap<T> {

    private static final int LENGTH = Version.values().length;
    private final Object[] data;

    protected AbstractVersionMap() {
        this.data = new Object[LENGTH];
        /*EnumMap map = null;
        map.computeIfAbsent()*/
    }

    abstract void setElement(Object[] data, int i, T val);

    abstract T getElement(Object[] data, int i);

    public final void forEach(Consumer<T> consumer) {
        for (int i = 0; i < this.data.length; i++) {
            T elem = this.getElement(this.data, i);
            if (elem != null) consumer.accept(elem);
        }
    }

    public final void put(Version version, T value) {
        Objects.requireNonNull(version);
        Objects.requireNonNull(value);
        this.setElement(this.data, version.ordinal(), value);
    }

    public final void putIfAbsent(Version version, T value) {
        if (!this.containsKey(version)) this.put(version, value);
    }

    public final T computeIfAbsent(Version version, Function<Version, T> function) {
        T value = this.get(version);
        if (value == null) this.put(version, value = function.apply(version));
        return value;
    }

    public final boolean containsKey(Version version) {
        return this.get(version) != null;
    }

    public final T get(Version version) {
        Objects.requireNonNull(version);
        return this.getElement(this.data, version.ordinal());
    }

    public final T getOrDefault(Version version, T def) {
        T val = this.get(version);
        return val != null ? val : def;
    }
}