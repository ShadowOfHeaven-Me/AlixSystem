package alix.common.utils.file.temp;

import alix.common.utils.file.AlixFileManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public abstract class TemporaryFile extends AlixFileManager {

    protected TemporaryFile(String fileName) {
        super(fileName);
    }
}