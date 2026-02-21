package ua.nanit.limbo.util.map;

import alix.common.utils.other.annotation.OptimizationCandidate;

public final class DefaultVersionMap<T> extends AbstractVersionMap<T> {

    //An alternative to an EnumMap
    public DefaultVersionMap() {
    }

    @Override
    void setElement(Object[] data, int i, T val) {
        data[i] = val;
    }

    @Override
    T getElement(Object[] data, int i) {
        return (T) data[i];
    }

    @OptimizationCandidate
    public static <T> DefaultVersionMap<T> filledWith(T entry) {
        DefaultVersionMap<T> map = new DefaultVersionMap<>();
        map.fill(entry);
        return map;
    }
}