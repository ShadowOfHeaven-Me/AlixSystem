package alix.common.utils.collections.fastutil;

import it.unimi.dsi.fastutil.longs.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * A concurrent set for 62 bit <b>unsigned</b> integers, that is all integers between 0 and 1L &lt;&lt; 62 - 1
 * (or 0x3FFF_FFFF_FFFF_FFFF). The two remaining bits are used for access control logic.
 *
 * <p>This set supports high level of concurrency similar that of {@link ConcurrentHashMap}, however unlike {@link ConcurrentHashMap}
 * {@link ConcurrentInt62Set} does not support resizing the amounts of buckets. Performance of this map degrades
 * if too few buckets are used for too many elements. The size of each bucket increases exponentially in factors of two
 * with a starting size of 16 elements. All buckets are initialized eagerly, that is either at creation time
 * or when {@link #clear()} is invoked. <b>Buckets cannot be shrunk down after expanding to a certain size. Only
 * {@link #clear()} (or discarding the set itself) would cause the allocated memory to be freed. The amount of buckets
 * is defined from the start and cannot be changed later on. For performance reasons, it must be a power of two.</b>
 *
 * <p>The atomic operations {@link #add(long)}, {@link #remove(long)} and {@link #contains(long)} are the smallest
 * unit from which the set can be altered. All other operations (aside from the iterator) are based on either the
 * atomic operations or the iterator. These operations are not atomic, that is the effects might not fully take effect.
 * For example a call to {@link #retainAll(LongCollection)} might remove a value added via {@link #add(long)} shortly after
 * calling {@link #retainAll(LongCollection)}. However, it is also possible that it keeps that value. Should
 * this atomic behaviour be required nonetheless, the set should be synchronised via {@link LongSets#synchronize(LongSet)}
 * or similar.
 *
 * <p>Even though {@link #add(long)} and {@link #remove(long)} are considered atomic, they are not guaranteed to be
 * non-blocking. During a resizing operation, a thread may need to wait on other threads in order to gain exclusive
 * control over a bucket's internal array. Conversely, once the lock has been acquired, other threads will need to wait
 * until the lock has been relinquished. This behaviour is required in order to guarantee that the effects of
 * {@link #add(long)} and {@link #remove(long)} persist even across a resize. To reduce the likelihood of this occurring,
 * a larger amount of buckets may need to be used. To expand on this it may be beneficial to redistribute the bits
 * of the longs so that the most significant bits are less frequent to occur in a given combination. This is especially
 * beneficial when storing smaller values in the set, as this will necessarily cause some buckets to be empty.
 *
 * <p>Multiple iterators are supported at the same time. Furthermore, this set implementation also supports modification
 * of the set without causing the iterators to fail - except if the modification would cause the iterator to be exhausted.
 * However, as iterators do not represent an atomic snapshot (or any snapshot at all for that matter) it is possible
 * that underlying changes may improperly reflect on the iteration results.
 * That is an {@link Iterator#next()} call can fail even though {@link Iterator#hasNext()} has been called beforehand
 * as a {@link LongSet#remove(long)} operation which was called in between hasNext and next caused the iterator to be
 * exhausted. Extreme care should be taken when using Iterators in concurrent environments involving additions to the
 * set coupled with removals.
 *
 * <p><b>This set does not implement {@link #hashCode()} and {@link #equals(Object)}. Should these methods be required nonetheless
 * a wrapper may be appropriate.</b>
 *
 * @author geolykt
 */
public final class ConcurrentInt62Set implements LongSet {

    //Source code: https://github.com/stianloader/stianloader-concurrent/blob/main/src/main/java/org/stianloader/concurrent/ConcurrentInt62Set.java

    final Bucket[] buckets;
    final int bucketCount;

    private static final long CTRL_BIT_READ = 1L << 63;
    private static final long INT_32_BITS = 0xFFFF_FFFFL;
    private static final long INT_63_BITS = ~CTRL_BIT_READ;
    private static final long INT_62_BITS = INT_63_BITS & ~(1L << 62);
    static final AtomicIntegerFieldUpdater<Bucket> BUCKET_SIZE = AtomicIntegerFieldUpdater.newUpdater(Bucket.class, "size");
    static final AtomicIntegerFieldUpdater<Bucket> BUCKET_CTRL = AtomicIntegerFieldUpdater.newUpdater(Bucket.class, "ctrl");

    private static int indexFor(long element, int size) {
        return (int) ((element & INT_32_BITS) ^ (element >> 32)) & (size - 1);
    }

    static final class Bucket {
        volatile int ctrl;
        volatile AtomicLongArray values;
        volatile int size;

        private void lockCtrl() {
            int ctrl;
            while (!BUCKET_CTRL.compareAndSet(this, ctrl = this.ctrl, -ctrl - 1));
            while (this.ctrl != -1) Thread.yield();
        }

        private void decrementCtrl() {
            int ctrl;
            while (!BUCKET_CTRL.compareAndSet(this, ctrl = this.ctrl, ctrl < 0 ? ctrl + 1 : ctrl - 1));
        }

        private void incrementCtrl() {
            int ctrl;
            do {
                while ((ctrl = this.ctrl) < 0) {
                    Thread.yield();
                }
            } while (!BUCKET_CTRL.compareAndSet(this, ctrl, ctrl + 1));
        }

        boolean contains(long value) {
            AtomicLongArray values = this.values;
            if (values == null) {
                return false;
            }
            value |= CTRL_BIT_READ;
            int index = values.length();
            while (index-- != 0) {
                if (values.get(index) == value) {
                    return true;
                }
            }
            return false;
        }

        boolean add(long element) {
            this.incrementCtrl();
            AtomicLongArray values = this.values;
            if (values == null) {
                this.decrementCtrl();
                this.growValues(values);
                return this.add(element);
            }


            int len = BUCKET_SIZE.incrementAndGet(this);
            if (len >= values.length()) {
                this.decrementCtrl();
                this.growValues(values);
                BUCKET_SIZE.decrementAndGet(this);
                return this.add(element);
            }

            int index = values.length();
            int storeIndex = -1;
            while (index-- != 0) {
                if (storeIndex == -1 && values.compareAndSet(index, 0, element)) {
                    storeIndex = index;
                } else {
                    if ((values.get(index) & ~CTRL_BIT_READ) == element) {
                        if (storeIndex >= 0) {
                            values.set(storeIndex, 0);
                        }
                        BUCKET_SIZE.decrementAndGet(this);
                        this.decrementCtrl();
                        return false;
                    }
                }
            }

            if (storeIndex >= 0) {
                if (!values.compareAndSet(storeIndex, element, element | CTRL_BIT_READ)) {
                    BUCKET_SIZE.decrementAndGet(this);
                    this.decrementCtrl();
                    throw new AssertionError("Unable to CAS back to read-enabled (but not write-disabled) state.");
                }
                this.decrementCtrl();
                return true;
            }

            BUCKET_SIZE.decrementAndGet(this);
            this.decrementCtrl();
            return this.add(element);
        }

        boolean remove(long element) {
            this.incrementCtrl();
            AtomicLongArray values = this.values;
            if (values == null) {
                this.decrementCtrl();
                return false;
            }

            int index = values.length();
            while (index-- != 0) {
                long value = values.get(index);
                if ((value & ~CTRL_BIT_READ) != element) {
                    continue;
                }
                if (!values.compareAndSet(index, value, 0)) {
                    index++;
                    continue;
                }
                BUCKET_SIZE.decrementAndGet(this);
                this.decrementCtrl();
                return true;
            }

            this.decrementCtrl();
            return false;
        }

        synchronized void growValues(AtomicLongArray witness) {
            if (this.values != witness) {
                return;
            }
            if (witness == null) {
                this.values = new AtomicLongArray(16);
                return;
            }

            this.lockCtrl();

            int len = witness.length();
            AtomicLongArray grown = new AtomicLongArray(len << 1);
            for (int i = 0; i < len; i++) {
                grown.set(i + len, witness.get(i));
            }
            this.values = grown;
            BUCKET_CTRL.incrementAndGet(this);
        }
    }

    public ConcurrentInt62Set(int bucketCount) {
        if (Integer.bitCount(bucketCount) != 1) {
            throw new IllegalArgumentException("bucketCount must be a power of 2.");
        }
        this.bucketCount = bucketCount;
        this.buckets = new Bucket[bucketCount];
        for (int i = 0; i < this.bucketCount; i++) {
            this.buckets[i] = new Bucket();
        }
    }

    public boolean add(long element) {
        if ((element & ~INT_62_BITS) != 0) {
            throw new IllegalArgumentException("Input element is not a 62-bit unsigned integer: DEC: " + element + " BINARY: " + Long.toBinaryString(element));
        }
        element++;
        return this.buckets[ConcurrentInt62Set.indexFor(element, bucketCount)].add(element);
    }

    public boolean remove(long element) {
        if ((element & ~INT_62_BITS) != 0) {
            throw new IllegalArgumentException("Input element is not a 62-bit unsigned integer: DEC: " + element + " BINARY: " + Long.toBinaryString(element));
        }
        element++;
        return this.buckets[ConcurrentInt62Set.indexFor(element, bucketCount)].remove(element);
    }

    public boolean contains(long element) {
//        Bucket[] buckets = this.buckets;
//        if (buckets == null) {
//            return false;
//        }
//        int size = buckets.length;
//        if (size == 0) {
//            return false;
//        }
        element++;
        return buckets[ConcurrentInt62Set.indexFor(element, bucketCount)].contains(element);
    }

    @Override
    public long[] toLongArray() {
        LongArrayList list = new LongArrayList();
        this.iterator().forEachRemaining(list::add);
        return list.toLongArray();
    }

    @Override
    public long[] toArray(long[] a) {
        // Not efficient either but does the job
        long[] array = this.toLongArray();
        if (array.length < a.length) {
            System.arraycopy(array, 0, a, 0, array.length);
            return a;
        } else {
            return array;
        }
    }

    @Override
    public boolean addAll(LongCollection c) {
        LongIterator lt = c.longIterator();
        boolean modified = false;
        while (lt.hasNext()) {
            modified |= this.add(lt.nextLong());
        }
        return modified;
    }

    @Override
    public boolean containsAll(LongCollection c) {
        LongIterator lt = c.longIterator();
        while (lt.hasNext()) {
            if (!this.contains(lt.nextLong())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(LongCollection c) {
        LongIterator lt = c.longIterator();
        boolean modified = false;
        while (lt.hasNext()) {
            modified |= this.remove(lt.nextLong());
        }
        return modified;
    }

    @Override
    public boolean retainAll(LongCollection c) {
        LongIterator it = this.longIterator();
        boolean modified = false;
        while (it.hasNext()) {
            if (!c.contains(it.nextLong())) {
                it.remove();
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public boolean addAll(Collection<? extends Long> c) {
        Iterator<? extends Long> lt = c.iterator();
        boolean modified = false;
        while (lt.hasNext()) {
            modified |= this.add(lt.next().longValue());
        }
        return modified;
    }

    @Override
    public int size() {
        int size = 0;
        for (Bucket b : this.buckets) {
            size += b.size;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Object[] toArray() {
        LongArrayList list = new LongArrayList();
        this.iterator().forEachRemaining(list::add);
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        LongArrayList list = new LongArrayList();
        this.iterator().forEachRemaining(list::add);
        return list.toArray(a);
    }

    @Override
    @Deprecated
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        LongIterator it = this.longIterator();
        boolean modified = false;
        while (it.hasNext()) {
            if (!c.contains(it.nextLong())) {
                it.remove();
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            if (o instanceof Number && this.remove(((Number) o).longValue())) {
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public void clear() {
        // Not that efficient but should do the job for now
        for (int i = 0; i < this.bucketCount; i++) {
            this.buckets[i] = new Bucket();
        }
    }

    @Override
    public LongIterator iterator() {
        return new LongIterator() {
            private final Bucket[] buckets = ConcurrentInt62Set.this.buckets;
            private int indexGlobal = -1;
            private int indexBucket;
            private int lastIdxG = -1;
            private AtomicLongArray currentBucketArray;
            private long lastValue;

            @Override
            public boolean hasNext() {
                if (this.indexGlobal == -1) {
                    this.indexGlobal = 0;
                    this.currentBucketArray = this.buckets[0].values;
                }
                if (this.indexGlobal >= this.buckets.length || this.currentBucketArray == null) {
                    return false;
                }
                int len = this.currentBucketArray.length();
                while ((this.currentBucketArray.get(this.indexBucket) & CTRL_BIT_READ) == 0) {
                    if (++this.indexBucket == len) {
                        if (++this.indexGlobal >= this.buckets.length) {
                            return false;
                        }
                        this.currentBucketArray = this.buckets[this.indexGlobal].values;
                        len = this.currentBucketArray.length();
                    }
                }
                return true;
            }

            @Override
            public long nextLong() {
                if (this.indexGlobal == -1) {
                    this.hasNext();
                }

                if (this.indexGlobal > this.buckets.length) {
                    throw new NoSuchElementException("Iterator exhausted. (Note: In very rare cases this can be caused by resizing internal Arrays. In other cases this can be traced back to concurrency problems. The usage of the iterator in concurrent environments is dangerous.)");
                }

                long val = this.currentBucketArray.get(this.indexBucket);
                while ((val & CTRL_BIT_READ) == 0) {
                    if (this.hasNext()) {
                        val = this.currentBucketArray.get(this.indexBucket);
                    } else {
                        throw new NoSuchElementException("Iterator exhausted. (Note: In very rare cases this can be caused by resizing internal Arrays. In other cases this can be traced back to concurrency problems. The usage of the iterator in concurrent environments is dangerous.)");
                    }
                }

                this.lastIdxG = this.indexGlobal;
                this.lastValue = val &= INT_63_BITS;

                if (++this.indexBucket == this.currentBucketArray.length()) {
                    this.indexBucket = 0;
                    if (++this.indexGlobal == this.buckets.length) {
                        this.currentBucketArray = null;
                    } else {
                        this.currentBucketArray = this.buckets[this.indexGlobal].values;
                    }
                }

                return val - 1;
            }

            @Override
            public void remove() {
                if (this.lastIdxG == -1) {
                    throw new IllegalStateException("#next() has not been called!");
                }

                if (!this.buckets[this.lastIdxG].remove(this.lastValue)) {
                    throw new IllegalStateException("Element already removed.");
                }
            }
        };
    }
}