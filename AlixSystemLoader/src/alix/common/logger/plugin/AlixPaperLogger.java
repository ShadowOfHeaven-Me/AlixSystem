package alix.common.logger.plugin;

import alix.common.logger.AlixLoggerProvider;
import com.destroystokyo.paper.utils.PaperPluginLogger;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class AlixPaperLogger extends Logger {
    public AlixPaperLogger() {
        super("xes", null);
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage(AlixLoggerProvider.LOGGER_NAME_FULL + record.getMessage());
        super.log(record);
    }

    //private final String prefix;

    /*public AlixPaperLogger() {
        this.setParent(BukkitAlixMain.instance.getServer().getLogger());
        this.setLevel(Level.ALL);
    }*/

/*    @Override
    public void log(LogRecord record) {
        //if(record.getLevel() == Level.WARNING)
        super.log(record);
    }*/

    /*    public final void log(LogRecord log) {
        log.setMessage(LOGGER_NAME + log.getMessage());
        super.log(log);
    }*/

    public static Logger createLogger() {
        return PaperPluginLogger.getLogger(new PluginMeta() {

            //TODO: Fix this
            @Override
            public @Nullable String getLoggerPrefix() {
                return AlixLoggerProvider.LOGGER_NAME_BRANCHLESS;
            }

            @Override
            public @NotNull String getName() {
                return "";
            }

            @Override
            public @NotNull String getMainClass() {
                return "";
            }

            @Override
            public @NotNull PluginLoadOrder getLoadOrder() {
                return null;
            }

            @Override
            public @NotNull String getVersion() {
                return "";
            }

            @Override
            public @NotNull List<String> getPluginDependencies() {
                return List.of();
            }

            @Override
            public @NotNull List<String> getPluginSoftDependencies() {
                return List.of();
            }

            @Override
            public @NotNull List<String> getLoadBeforePlugins() {
                return List.of();
            }

            @Override
            public @NotNull List<String> getProvidedPlugins() {
                return List.of();
            }

            @Override
            public @NotNull List<String> getAuthors() {
                return List.of();
            }

            @Override
            public @NotNull List<String> getContributors() {
                return List.of();
            }

            @Override
            public @Nullable String getDescription() {
                return "";
            }

            @Override
            public @Nullable String getWebsite() {
                return "";
            }

            @Override
            public @NotNull List<Permission> getPermissions() {
                return List.of();
            }

            @Override
            public @NotNull PermissionDefault getPermissionDefault() {
                return null;
            }

            @Override
            public @Nullable String getAPIVersion() {
                return "";
            }
        });
    }
}