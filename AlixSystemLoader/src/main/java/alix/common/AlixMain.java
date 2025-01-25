package alix.common;

import alix.loaders.classloader.LoaderBootstrap;

import java.io.File;
import java.nio.file.Path;

public interface AlixMain {

    Path getDataFolderPath();

    default File getDataFolder() {
        return new File(this.getDataFolderPath().toUri());
    }

    LoaderBootstrap getBootstrap();

    Params getEngineParams();

    interface Params {

        String messagesFileName();

    }
}