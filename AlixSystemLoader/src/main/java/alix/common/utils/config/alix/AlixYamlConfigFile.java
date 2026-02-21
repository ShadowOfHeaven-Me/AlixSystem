package alix.common.utils.config.alix;

import alix.common.AlixCommonMain;
import alix.common.utils.file.AlixFileManager;
import lombok.SneakyThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class AlixYamlConfigFile extends AlixFileManager {

    final Map<String, String> values = new HashMap<>();
    final Map<String, List<String>> lists = new HashMap<>();
    private int linesRead;

    AlixYamlConfigFile(File file) {
        super(file);
    }

    @SneakyThrows
    void loadConfig() {
        this.load(true);
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
                AlixCommonMain.logError("Line number " + this.linesRead + ", '" + line + "' contains no ':' separator symbol!");
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