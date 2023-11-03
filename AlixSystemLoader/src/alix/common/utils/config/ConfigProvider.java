package alix.common.utils.config;

import alix.common.utils.config.bukkit.BukkitConfigImpl;

public interface ConfigProvider {

    int getInt(String s);

    boolean getBoolean(String s);

    String getString(String s);

    ConfigProvider config = new BukkitConfigImpl();

}