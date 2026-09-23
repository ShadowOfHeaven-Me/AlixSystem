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

        /**
         * The messages file treated as the "reference" translation to validate {@link #messagesFileName()}
         * against for missing keys (see Messages' startup check) - i.e. the language every other
         * bundled translation is expected to have every key of. Defaults to {@link #messagesFileName()}
         * itself, meaning "nothing to compare against" for a platform with only a single messages file;
         * a platform offering multiple language files (like Velocity's "language" config option) should
         * override this to always return its default/canonical language's file name, regardless of which
         * one is actually selected.
         */
        default String referenceMessagesFileName() {
            return messagesFileName();
        }
    }
}