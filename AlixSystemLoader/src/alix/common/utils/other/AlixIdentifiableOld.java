/*
package alix.common.utils.other;

import java.util.concurrent.locks.ReentrantLock;

//An class used for identifying a

public class AlixIdentifiable {

    private static int nextID;
    private final int id = ++nextID; //to ensure the hashCode isn't 0, which is the general hash code used for all null instances (also might be slightly faster)

    @Override
    public final boolean equals(Object o) {
        return o != null && o.hashCode() == this.hashCode() && o instanceof AlixIdentifiable;//it's faster to check for hash code equality rather than instanceof - instanceof will only be used to confirm the object identity
    }

    @Override
    public final int hashCode() {
        return this.id;
    }
}*/
