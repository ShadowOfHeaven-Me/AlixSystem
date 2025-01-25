package alix.common.utils.collections.list;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ConcurrentCacheList<T> {

    private volatile Object[] data;
    private final AtomicInteger index;
    private final int newArraySizeThreshold;

    public ConcurrentCacheList(int predictedMaxSize, int newArraySizeThreshold) {
        this.data = new Object[predictedMaxSize];
        this.index = new AtomicInteger();
        this.newArraySizeThreshold = newArraySizeThreshold;
    }

    public void add(@NotNull T value) {

    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    public void drain(Consumer<T> t) {
        if (index.get() >= newArraySizeThreshold) {
            Object[] a = this.data;
            this.data = new Object[this.data.length];
        }
    }
}