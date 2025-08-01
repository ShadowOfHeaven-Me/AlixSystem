package alix.common.data.file;

import alix.common.utils.file.AlixFileManager;
import alix.common.utils.other.throwable.AlixException;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AllowListFile extends AlixFileManager {

    private final Set<String> names = ConcurrentHashMap.newKeySet();

    AllowListFile() {
        super("allow-list.txt", FileType.CONFIG);
    }

    @Override
    protected void loadLine(String line) {
        this.names.add(line);
    }

    void save() {
        try {
            super.save0(this.names);
        } catch (IOException e) {
            throw new AlixException(e);
        }
    }

    Set<String> getNames() {
        return names;
    }
}