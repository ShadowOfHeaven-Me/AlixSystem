package ua.nanit.limbo.util.map;

import alix.common.utils.other.annotation.OptimizationCandidate;

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

    @OptimizationCandidate
    public static <T> VersionMap<T> filledWith(T entry) {
        VersionMap<T> map = new VersionMap<>();
        map.fill(entry);
        return map;
    }
}