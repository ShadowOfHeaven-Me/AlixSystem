package alix.common.login.premium;

import alix.common.AlixCommonMain;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import static alix.common.utils.config.ConfigProvider.config;

public enum EncryptionSetting {

    ALWAYS,
    SAFE,
    NEVER;

    public static boolean enableEncryption(ClientVersion version) {
        return switch (Config.setting) {
            case SAFE -> version.isNewerThanOrEquals(ClientVersion.V_1_20_5);
            case NEVER -> false;
            case ALWAYS -> true;
        };
    }

    private static final class Config {

        //private static final boolean requirePremiumWhenNoSuggestion = config.getBoolean("require-premium-when-no-suggestion");
        private static final EncryptionSetting setting = config();

        private static EncryptionSetting config() {
            String cfg = config.getString("encrypt-connection");
            try {
                return valueOf(cfg.toUpperCase());
            } catch (IllegalArgumentException e) {
                AlixCommonMain.logWarning("Invalid config parameter 'encrypt-connection', got: '" + cfg + "', for \"never, safe, always\" being the only valid options!");
                return SAFE;//config default
            }
        }
    }

}