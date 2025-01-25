package alix.common.utils.config.velocity;

import alix.common.utils.config.ConfigProvider;
import alix.loaders.velocity.VelocityAlixMain;
import org.bukkit.configuration.file.YamlConfiguration;

public final class VelocityConfigImpl implements ConfigProvider {

    private final YamlConfiguration config = VelocityAlixMain.instance.getConfig();

    @Override
    public int getInt(String s) {
        return config.getInt(s);
    }

    @Override
    public boolean getBoolean(String s) {
        return config.getBoolean(s);
    }

    @Override
    public String getString(String s) {
        return config.getString(s);
    }
}