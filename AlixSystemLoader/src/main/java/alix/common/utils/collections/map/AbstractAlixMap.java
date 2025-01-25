/*
package alix.common.utils.collections.map;

public class AbstractAlixMap<K, V> {


    public AbstractAlixMap(int size) {

    }

    public static int calculateMapCapacity(int minCapacity) {
        if (minCapacity <= 0)
            throw new IllegalArgumentException("Minimum capacity (" + minCapacity + ") must be positive!");
        if ((minCapacity & (minCapacity - 1)) == 0) return minCapacity;//the given argument is already a power of 2
        if (minCapacity == Integer.MAX_VALUE)
            throw new OutOfMemoryError("Cannot allocate " + minCapacity + " of map slots");
        for (int i = 1; i < 31; i++) {
            int powerOf2 = 1 << i;

            if (powerOf2 > minCapacity) return powerOf2;
        }
        throw new InternalError("Could not allocate map slots for the amount of " + minCapacity + ".");
    }
}*/
