package alix.common.utils.config;

public final class ConfigParams {

    public static final int maximumTotalAccounts;
    public static final boolean isDebugEnabled, isCaptchaParticle;

    static {
        ConfigProvider config = ConfigProvider.config;
        maximumTotalAccounts = config.getInt("max-total-accounts");
        isDebugEnabled = config.getBoolean("debug");
        isCaptchaParticle = config.getString("captcha-visual-type").equalsIgnoreCase("particle");
    }
}