package alix.common.data.settings;

import java.util.Objects;

public final class ServerSettingsManager {

    static final ServerSettings settings = new ServerSettings();

    static {
        settings.loadExceptionless();
    }

    public static <T> boolean is(Setting<T> setting, T value) {
        return Objects.equals(get(setting), value);
    }

    public static <T> void set(Setting<T> setting, T value) {
        settings.set(setting, value);
    }

    public static <T> T get(Setting<T> setting) {
        return (T) settings.data[setting.ordinal()];
    }

    public static void init() {
    }

    /**
     * v1.5.2 ("/as reload") - see ServerSettings#reload() for why this can't just be
     * settings.loadExceptionless() again.
     */
    public static void reload() {
        settings.reload();
    }
}