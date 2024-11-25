package nanolimbo.alix.util.map;

import alix.common.utils.collections.list.AtomicArrayUtil;

public final class ConcurrentVersionMap<T> extends AbstractVersionMap<T> {

    public ConcurrentVersionMap() {
    }

    @Override
    protected void setElement(Object[] data, int i, T val) {
        AtomicArrayUtil.setElement(data, i, val);
    }

    @Override
    protected T getElement(Object[] data, int i) {
        return (T) AtomicArrayUtil.getElement(data, i);
    }
}