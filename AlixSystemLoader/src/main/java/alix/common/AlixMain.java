package alix.common;

import alix.loaders.classloader.LoaderBootstrap;

import java.io.File;
import java.nio.file.Path;

public interface AlixMain {

    Path getDataFolderPath();

    default File getDataFolder() {
        File dataFolder = new File(this.getDataFolderPath().toUri());
        if (!dataFolder.exists()) dataFolder.mkdir();
        return dataFolder;
    }

    LoaderBootstrap getBootstrap();

    Params getEngineParams();

    interface Params {

        String messagesFileName();

        default char messagesSeparator() {
            return ':';
        }
    }
}