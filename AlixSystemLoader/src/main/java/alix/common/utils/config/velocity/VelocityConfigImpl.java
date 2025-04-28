package alix.common.utils.config.velocity;

import alix.common.utils.config.ConfigProvider;
import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.file.AlixFileManager;

public final class VelocityConfigImpl implements ConfigProvider {

    private final AlixYamlConfig config = new AlixYamlConfig(AlixFileManager.getOrCreatePluginFile("config.yml", AlixFileManager.FileType.CONFIG));

    @Override
    public int getInt(String s) {
        return this.config.getInt(s);
    }

    @Override
    public int getInt(String s, int def) {
        return this.config.getInt(s, def);
    }

    @Override
    public boolean getBoolean(String s) {
        return this.config.getBoolean(s);
    }

    @Override
    public boolean getBoolean(String s, boolean def) {
        return this.config.getBoolean(s, def);
    }

    @Override
    public String getString(String s) {
        return this.config.getString(s);
    }
}