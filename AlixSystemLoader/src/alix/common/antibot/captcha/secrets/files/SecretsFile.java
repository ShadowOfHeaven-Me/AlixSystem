package alix.common.antibot.captcha.secrets.files;

import alix.common.utils.file.AlixFileManager;
import org.bukkit.configuration.file.YamlConfiguration;

public final class SecretsFile extends AlixFileManager {

    private final YamlConfiguration config;

    SecretsFile() {
        super("secrets.yml", FileType.SECRET);
        this.config = YamlConfiguration.loadConfiguration(this.getFile());
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    @Override
    protected void loadLine(String line) {
        throw new UnsupportedOperationException();
    }
}