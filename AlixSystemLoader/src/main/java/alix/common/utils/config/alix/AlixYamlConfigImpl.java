package alix.common.utils.config.alix;

import alix.common.AlixCommonMain;
import alix.common.utils.file.AlixFileManager;
import lombok.SneakyThrows;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

final class AlixYamlConfigImpl extends AlixFileManager {

    final Map<String, String> values = new HashMap<>();
    private int linesRead;

    AlixYamlConfigImpl(File file) {
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
            AlixCommonMain.logError("Line number " + this.linesRead + ", '" + line + "' contains no ':' separator symbol!");
            return;
        }
        this.values.put(a[0], a[1]);
    }
}