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

    /**
     * v1.5.2 ("/as reload"): re-reads allow-list.txt in place - safe to call again (loadLine() above
     * only ever does Set#add(), overwrite-safe). One caveat: a name manually DELETED from the file on
     * disk (rather than through "/as bl-r") stays in memory until a restart - this only ever adds
     * entries back, it never removes ones no longer present in the file, matching how this class already
     * behaves for every other in-memory-Set-backed file in this codebase.
     */
    public static void reload() {
        file.loadExceptionless();
    }

    static {
        file.loadExceptionless();
    }
}