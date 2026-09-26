package alix.common.utils.config;

import alix.common.data.LoginType;

public final class ConfigParams {

    public static final int maximumTotalAccounts, maxLoginTime, fingerprintingPort, emailVerificationTime;
    public static final boolean isDebugEnabled, isCaptchaMap, playerIPAutoLogin, forcefullyDisableAutoLogin, hasMaxLoginTime,
            requireRegisterFromAll, loadBuiltInIps, checkBreachedPasswords, fingerprintingEnabled, hasEmailVerificationTime,
            supportMobileConnections;
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
        requireRegisterFromAll = config.getBoolean("require-register-from-all");
        loadBuiltInIps = config.getBoolean("load-builtin-ips");
        hasMaxLoginTime = maxLoginTime > 0;
        checkBreachedPasswords = config.getBoolean("check-breached-passwords");
        fingerprintingPort = config.getInt("fingerprinting-port");
        fingerprintingEnabled = fingerprintingPort > 0;
        //A separate, typically longer, countdown used only while a 'require-email-in-register' registration
        //is waiting on the verification email to arrive (see LoginState#handleRegisterCommandWithEmail()) -
        //receiving an email can easily take longer than max-login-time allows for the rest of the
        //login/register GUI, previously meaning a slow-arriving email could kick the player before they
        //even had a code to enter, forcing them to restart registration (and wait for a new email) every
        //time. Falls back to 0 (disabled, same as max-login-time's own hasMaxLoginTime pattern) if unset -
        //this key doesn't exist in every platform's config.yml, the same as 'require-email-in-register' itself.
        emailVerificationTime = config.getInt("email-verification-time");
        hasEmailVerificationTime = emailVerificationTime > 0;
        //Gates the CELLULAR_CARRIER MTU signature - see MtuAnalyser#guessMtuEnvironment()'s docs.
        supportMobileConnections = config.getBoolean("support-mobile-connections");

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