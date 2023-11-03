package alix.common.utils.config;

public class ConfigParams {

    public static final int maximumTotalAccounts;

    static {
        ConfigProvider config = ConfigProvider.config;
        maximumTotalAccounts = config.getInt("max-total-accounts");
    }
}