package alix.bungee;

import alix.pluginloader.LoaderBootstrap;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.YamlConfiguration;

import java.util.logging.Logger;

public class Main implements LoaderBootstrap {

    private final Plugin plugin;

    public Main(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}