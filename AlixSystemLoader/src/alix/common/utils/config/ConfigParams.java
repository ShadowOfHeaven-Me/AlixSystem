package alix.common.utils.config;

import alix.common.data.LoginType;

public final class ConfigParams {

    public static final int maximumTotalAccounts;
    public static final boolean isDebugEnabled, isCaptchaMap, playerIPAutoLogin;
    public static final LoginType defaultLoginType;

    static {
        ConfigProvider config = ConfigProvider.config;
        maximumTotalAccounts = config.getInt("max-total-accounts");
        isDebugEnabled = config.getBoolean("debug");
        isCaptchaMap = config.getString("captcha-visual-type").equalsIgnoreCase("map");
        playerIPAutoLogin = config.getBoolean("auto-login");
        defaultLoginType = LoginType.from(config.getString("password-type").toUpperCase(), true);

        /*String loginType = config.getString("password-type").toLowerCase();
        *//*switch (loginType) {
            case "password":
            case "command":
            case "pin":
            case "anvil_password":
            case "anvil":
                break;
            default:
                loginType = "command";
                AlixCommonMain.logWarning("Invalid 'password-type' parameter set in config! Available: password & pin, but instead got '" +
                        loginType + "! Switching to 'command', as default.");
                break;
        }*//*
        defaultLoginType = LoginType.from(loginType.toUpperCase(), true);*/

        /*LoginType type;

        LoaderBootstrap bootstrap = AlixCommonMain.MAIN_CLASS_INSTANCE.getBootstrap();
        try {
            type = (LoginType) bootstrap.getClass().getMethod("getConfigLoginType").invoke(bootstrap);
        } catch (Exception e) {
            throw new AlixException(e);
        }
        defaultLoginType = type;*/
    }
}