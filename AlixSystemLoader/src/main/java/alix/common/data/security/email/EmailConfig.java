package alix.common.data.security.email;

import alix.common.utils.config.alix.AlixYamlConfig;
import alix.common.utils.file.AlixFileManager;

public final class EmailConfig {

    public static final EmailConfig INSTANCE = new EmailConfig();
    private final AlixYamlConfig config;
    public final String host, username, password, email, sender;
    public final int port;

    EmailConfig() {
        var file = AlixFileManager.getOrCreatePluginFile("email-config.yml", AlixFileManager.FileType.CONFIG);
        this.config = new AlixYamlConfig(file);
        this.host = config.getString("host");
        this.username = config.getString("username");
        this.password = config.getString("password");
        this.email = config.getString("email");
        this.sender = config.getString("sender");
        this.port = config.getInt("port");
    }

    public static void init() {
    }
}