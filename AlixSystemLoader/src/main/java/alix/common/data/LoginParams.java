package alix.common.data;

import alix.common.data.security.password.Password;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LoginParams {

    private volatile Password password, extraPassword;
    private volatile LoginType loginType, extraLoginType;
    private volatile Boolean ipAutoLogin;
    private volatile AuthSetting authSettings;
    private volatile boolean hasProvenAuthAccess;

    LoginParams(String line) {
        String[] a = line.split(";");
        this.password = Password.readFromSaved(a[0]);
        if (a.length >= 2) {
            this.extraPassword = Password.readFromSaved(a[1]);
        }
    }

    LoginParams(Password password) {
        this.password = password;
        this.loginType = ConfigParams.defaultLoginType;
        this.authSettings = AuthSetting.PASSWORD;
    }

    void initLoginTypes(String data) {
        String[] a = data.split(";");
        this.loginType = AlixCommonUtils.readLoginType(a[0], ConfigParams.defaultLoginType);
        if (a.length == 2) this.extraLoginType = AlixCommonUtils.readLoginType(a[1], null);
    }

    void initSettings(String settings) {
        this.ipAutoLogin = settings.equals("0") || settings.equals("null") ? null : Boolean.parseBoolean(settings);
    }

    void initAuthSettings(String authSettings) {
        String[] a = authSettings.split(";");
        this.authSettings = AuthSetting.fromString(a[0]);
        if (a.length == 2) this.hasProvenAuthAccess = a[1].equals("1");
    }

    public String passwordsToSavable() {
        if (extraPassword == null) return password.toSavable();
        return password.toSavable() + ";" + extraPassword.toSavable();
    }

    public String loginTypesToSavable() {
        if (extraLoginType == null) return String.valueOf(loginType);
        return loginType + ";" + extraLoginType;
    }

    public String ipAutoLoginToSavable() {
        return String.valueOf(ipAutoLogin);
    }

    public String authSettingsToSavable() {
        return this.authSettings.toSavable() + ";" + (hasProvenAuthAccess ? "1" : "0");
    }

    public void setAuthSettings(AuthSetting authSettings) {
        this.authSettings = authSettings;
    }

    @NotNull
    public AuthSetting getAuthSettings() {
        return authSettings;
    }

    public void setHasProvenAuthAccess(boolean hasProvenAuthAccess) {
        this.hasProvenAuthAccess = hasProvenAuthAccess;
    }

    public boolean hasProvenAuthAccess() {
        return hasProvenAuthAccess;
    }

    public boolean isDoubleVerificationEnabled() {
        return extraLoginType != null;
    }

    public boolean getIpAutoLogin() {
        return ipAutoLogin == null ? ConfigParams.playerIPAutoLogin : ipAutoLogin;//default to config if not set manually by the user
    }

    public void setIpAutoLogin(boolean ipAutoLogin) {
        this.ipAutoLogin = ipAutoLogin;
    }

    @NotNull
    public Password getPassword() {
        return password;
    }

    public void setPassword(@NotNull Password password) {
        this.password = password;
    }

    @Nullable
    public Password getExtraPassword() {
        return extraPassword;
    }

    public void setExtraPassword(@Nullable Password extraPassword) {
        this.extraPassword = extraPassword;
    }

    LoginType getLoginType() {
        return loginType;
    }

    void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }

    public LoginType getExtraLoginType() {
        return extraLoginType;
    }

    public void setExtraLoginType(LoginType extraLoginType) {
        this.extraLoginType = extraLoginType;
    }
}