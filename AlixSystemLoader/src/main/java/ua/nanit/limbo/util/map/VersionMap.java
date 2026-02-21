package ua.nanit.limbo.util.map;

import ua.nanit.limbo.protocol.registry.Version;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface VersionMap<T> {

    void put(Version version, T value);

    T computeIfAbsent(Version version, Function<Version, T> function);

    boolean containsKey(Version version);

    T get(Version version);

    T getOrDefault(Version version, T def);

    void forEach(Consumer<T> consumer);

    Collection<T> valuesSnapshot();

}