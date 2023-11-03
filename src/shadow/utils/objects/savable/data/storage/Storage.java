package shadow.utils.objects.savable.data.storage;

import shadow.utils.main.AlixUtils;

import java.util.Collection;

public abstract class Storage {

    public static final String storageValuesSeparator = "~-~";
    private final String name, classDirectory;
    private final int finalLength;

    protected Storage(String name) {
        this.name = name;
        this.classDirectory = AlixUtils.getClassDirectory(this.getClass());
        finalLength = name.length() + classDirectory.length() + 6;
    }

    private static String[] skipStorageInfoData(String[] data) {
        return AlixUtils.skipArray(data, 2);
    }

    public static Storage of(String data) {
        String[] array = AlixUtils.split(data, storageValuesSeparator);
        String name = array[0];
        String classDirectory = array[1];
        Class<? extends Storage> c = forName(classDirectory);
        Storage s = newInstance(c, name);
        String[] values = skipStorageInfoData(array);
        return s.loadData(values);
    }
/*        switch (type) {
            case MAP:
                return new MapStorage<>(array[0]).loadData(values);
            case ARRAY:
                return new ArrayStorage<>(array[0]).loadData(values);
            case SINGLE:
                return new SingleValueStorage<>(array[0]).loadData(values);
        }*/
    //throw new RuntimeException("Unsupported StorageType: '" + type.name() + "'!");


    @SuppressWarnings("unchecked")
    private static Class<? extends Storage> forName(String name) {
        try {
            return (Class<? extends Storage>) Class.forName(name);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Storage newInstance(Class<? extends Storage> clazz, String name) {
        try {
            return clazz.getConstructor(String.class).newInstance(name);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public String toSavable() {
        Collection<?> values = values();
        int initialLength = finalLength + 4 * values.size();
        StringBuilder sb = new StringBuilder(initialLength).append(name).append(storageValuesSeparator).append(classDirectory).append(storageValuesSeparator);
        for (Object o : values)
            sb.append(o.toString()).append(storageValuesSeparator);
        return sb.substring(0, sb.length() - 3);
    }

    public final String getName() {
        return name;
    }

    public abstract Storage loadData(String[] data);

    public abstract Collection<?> values();
}