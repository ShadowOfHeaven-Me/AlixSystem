package alix.common.utils.config;

import alix.common.data.LoginType;

public final class ConfigParams {

    public static final int maximumTotalAccounts, maxLoginTime;
    public static final boolean isDebugEnabled, isCaptchaMap, playerIPAutoLogin, forcefullyDisableAutoLogin, hasMaxLoginTime;
    public static final LoginType defaultLoginType;

    static {
        ConfigProvider config = ConfigProvider.config;
        maximumTotalAccounts = config.getInt("max-total-accounts");
        isDebugEnabled = config.getBoolean("debug");
        isCaptchaMap = false; //config.getString("captcha-visual-type").equalsIgnoreCase("map");
        forcefullyDisableAutoLogin = config.getBoolean("forcefully-disable-auto-login");
        playerIPAutoLogin = config.getBoolean("auto-login") && !forcefullyDisableAutoLogin;
        defaultLoginType = LoginType.from(config.getString("password-type").toUpperCase(), true);
        maxLoginTime = config.getInt("max-login-time");
        hasMaxLoginTime = maxLoginTime > 0;

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