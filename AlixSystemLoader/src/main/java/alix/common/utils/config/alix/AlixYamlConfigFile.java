package alix.common.utils.config.alix;

import alix.common.AlixCommonMain;
import alix.common.utils.file.AlixFileManager;
import lombok.SneakyThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

final class AlixYamlConfigFile extends AlixFileManager {

    //v1.5.2 ("/as reload"): every loaded config.yml/database.yml/gui-menus/etc instance registers
    //itself here so a reload can find and re-parse ALL of them, without each caller needing to keep
    //its own reference around just for that purpose. A handful of entries added over a server's
    //lifetime (one per distinct file actually opened) - not a meaningful memory concern.
    private static final List<AlixYamlConfigFile> INSTANCES = new CopyOnWriteArrayList<>();

    final Map<String, String> values = new HashMap<>();
    final Map<String, List<String>> lists = new HashMap<>();
    private int linesRead;

    AlixYamlConfigFile(File file) {
        super(file);
        INSTANCES.add(this);
    }

    @SneakyThrows
    void loadConfig() {
        this.load(true);
    }

    /**
     * v1.5.2: re-reads this file from disk in place. MUST clear both maps first - loadLine() below
     * uses lists.computeIfAbsent(key, ...).add(...) for dash-list entries (e.g. gui-menus' "item-order"),
     * which is append-only; calling load() a second time without clearing would silently DUPLICATE every
     * list entry instead of refreshing it (the exact same class of bug already found and fixed once in
     * FileUpdater's disk-merge logic - see that class's dash-list handling). "values" (a plain HashMap,
     * put() overwrites) wouldn't have broken either way, but is cleared too so a key REMOVED from the
     * file on disk is also correctly forgotten, not left stale.
     */
    @SneakyThrows
    void reload() {
        this.values.clear();
        this.lists.clear();
        this.mostRecentKey = null;
        this.linesRead = 0;
        this.load(true);
    }

    static void reloadAll() {
        for (AlixYamlConfigFile file : INSTANCES) file.reload();
    }

    @Override
    protected void loadLine(String line) {
        this.linesRead++;
        line = line.trim();

        if (line.startsWith("#") || line.isBlank()) return;
        String[] a = line.split(":", 2);

        if (a.length == 1) {
            String[] list = line.split("- ", 2);

            if (list.length == 1 || this.mostRecentKey == null) {
                AlixCommonMain.logError("Line number " + this.linesRead + ", '" + line + "' contains no ':' separator symbol in " + this.getFile().getName() + "!");
                return;
            }
            this.lists.computeIfAbsent(this.mostRecentKey, k -> new ArrayList<>()).add(list[1].trim());
            return;
        }

        String key = a[0];
        String value = a[1];
        /*if (value.trim().startsWith(" - ")) {
            String list = value.split(" - ", 2)[1].trim();
            this.lists.computeIfAbsent(key, k -> new ArrayList<>()).add(list);
            return;
        }*/

        this.mostRecentKey = key;
        this.values.put(key, value);
    }

    private String mostRecentKey;

}