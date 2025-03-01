package alix.common.utils.config.bukkit;

import alix.loaders.bukkit.BukkitAlixMain;
import alix.common.utils.config.ConfigProvider;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BukkitConfigImpl implements ConfigProvider {

    private final YamlConfiguration config = (YamlConfiguration) BukkitAlixMain.instance.getConfig();

    @Override
    public int getInt(String s) {
        return config.getInt(s);
    }

    @Override
    public int getInt(String s, int def) {
        return config.getInt(s, def);
    }

    @Override
    public boolean getBoolean(String s) {
        return config.getBoolean(s);
    }

    @Override
    public boolean getBoolean(String s, boolean def) {
        return config.getBoolean(s, def);
    }

    @Override
    public String getString(String s) {
        return config.getString(s);
    }
}