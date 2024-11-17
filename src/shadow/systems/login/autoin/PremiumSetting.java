package shadow.systems.login.autoin;

import alix.common.data.PersistentUserData;
import alix.common.utils.other.throwable.AlixError;
import shadow.Main;

public enum PremiumSetting {
    NEVER,
    UNREGISTERED,
    ALWAYS;

    public static boolean requirePremium(PersistentUserData data) {
        return requirePremium(PersistentUserData.isRegistered(data));
    }

    public static boolean requirePremium(boolean isRegistered) {
        switch (Config.setting) {
            case NEVER:
                return false;
            case ALWAYS:
                return true;
            case UNREGISTERED:
                return !isRegistered;
            default:
                throw new AlixError("Invalid: " + Config.setting);
        }
    }

    private static final class Config {

        private static final PremiumSetting setting = config();

        private static PremiumSetting config() {
            String config = Main.config.getString("require-ownership-of-premium-nickname");
            try {
                return valueOf(config.toUpperCase());
            } catch (IllegalArgumentException e) {
                Main.logWarning("Invalid config parameter 'require-ownership-of-premium-nickname', got: '" + config + "', for \"never, unregistered, always\" being the only valid options!");
                return NEVER;
            }
        }
    }
}