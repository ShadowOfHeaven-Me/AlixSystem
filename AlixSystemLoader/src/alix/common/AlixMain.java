package alix.common;

import alix.loaders.classloader.LoaderBootstrap;

import java.nio.file.Path;

public interface AlixMain {

    Path getDataFolderPath();

    LoaderBootstrap getBootstrap();

    Params getEngineParams();

    interface Params {

        String messagesFileName();

    }
}