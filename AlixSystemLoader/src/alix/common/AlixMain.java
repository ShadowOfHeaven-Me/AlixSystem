package alix.common;

import alix.pluginloader.LoaderBootstrap;

import java.nio.file.Path;

public interface AlixMain {

    Path getDataFolderPath();

    LoaderBootstrap getBootstrap();

}