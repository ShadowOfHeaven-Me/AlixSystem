package alix.common.data.file;

public final class AllowListFileManager {

    private static final AllowListFile file = new AllowListFile();

    public static void add(String name) {
        file.getNames().add(name);
    }

    public static boolean has(String name) {
        return file.getNames().contains(name);
    }

    public static boolean remove(String name) {
        return file.getNames().remove(name);
    }

    public static void save() {
        file.save();
    }

    public static void init() {
    }

    static {
        file.loadExceptionless();
    }
}