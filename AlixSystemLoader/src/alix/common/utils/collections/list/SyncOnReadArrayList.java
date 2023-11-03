package alix.common.utils.collections.list;

import java.util.ArrayList;
import java.util.Iterator;

public class SyncOnReadArrayList<T> extends ArrayList<T> {

    @Override
    public Iterator<T> iterator() {
        return super.iterator();
    }

    private final class SyncedIterator {

    }

}