package alix.common.data.settings;

public final class ServerSettingsManager {

    static final ServerSettings settings = new ServerSettings();

    static {
        settings.loadExceptionless();
    }

    public static <T> void set(Setting setting, T value) {
        settings.set(setting, value);
    }

    public static <T> T get(Setting<T> setting) {
        return (T) settings.data[setting.ordinal()];
    }

    public static void init() {
    }
}