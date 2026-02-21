/*
package alix.velocity.utils.dependencies;

import alix.loaders.velocity.VelocityAlixMain;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.VelocityLibraryManager;
import com.nukkitx.natives.util.LibraryLoader;
import com.velocitypowered.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class LibbyManager {

    public static void loadDependencies(VelocityAlixMain plugin, @NotNull Logger logger, @NotNull Path dataDirectory, @NotNull PluginManager pluginManager) {
        var manager = new VelocityLibraryManager<>(plugin, logger, dataDirectory, pluginManager);
        manager.addMavenCentral();
        manager.addRepository("https://repo.codemc.io/repository/maven-releases/");
        manager.addRepository("https://repo.codemc.io/repository/maven-snapshots/");
        manager.addRepository("https://jitpack.io/");
        manager.addRepository("https://oss.sonatype.org/content/repositories/snapshots");
        manager.addRepository("https://oss.sonatype.org/content/repositories/central");

        var prefix = "alix{}libs";

        Library lib = Library.builder()
                .groupId("com{}github{}retrooper")
                .artifactId("packetevents-velocity")
                .version("2.9.5-SNAPSHOT")
                .resolveTransitiveDependencies(true)
                .excludeTransitiveDependency("net{}kyori","adventure-api")
                .excludeTransitiveDependency("net{}kyori","adventure-nbt")
                .relocate("io{}github{}retrooper{}packetevents", prefix + "{}io{}github{}retrooper{}packetevents")
                .relocate("com{}github{}retrooper{}packetevents", prefix + "{}com{}github{}retrooper{}packetevents")
                .build();
        manager.loadLibrary(lib);
    }
}
*/
