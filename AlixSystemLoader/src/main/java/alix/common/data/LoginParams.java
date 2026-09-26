package alix.common.data;

import alix.common.data.security.password.Password;
import alix.common.database.DatabaseUpdater;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LoginParams {

    private static final DatabaseUpdater database = DatabaseUpdater.INSTANCE;

    private final PersistentUserData data;
    private volatile Password password, extraPassword;
    private volatile LoginType loginType, extraLoginType;
    private volatile Boolean ipAutoLogin;
    private volatile AuthSetting authSettings;
    private volatile boolean hasProvenAuthAccess;

    LoginParams(PersistentUserData data, String line) {
        this.data = data;
        String[] a = line.split(";");
        this.password = Password.readFromSaved(a[0]);
        if (a.length >= 2) {
            this.extraPassword = Password.readFromSaved(a[1]);
        }
    }

    LoginParams(PersistentUserData data, Password password) {
        this.data = data;
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
        database.updateAuthSettingsByName(this.name(), authSettings);
    }

    public void setHasProvenAuthAccess(boolean hasProvenAuthAccess) {
        this.hasProvenAuthAccess = hasProvenAuthAccess;
        database.updateHasProvenAuthAccessByName(this.name(), hasProvenAuthAccess);
    }

    public void setIpAutoLogin(boolean ipAutoLogin) {
        this.ipAutoLogin = ipAutoLogin;
        database.updateIpAutoLoginByName(this.name(), ipAutoLogin);
    }

    public void setPassword(@NotNull Password password) {
        this.password = password;

        database.setPassword(this.name(), password, true);
    }

    public void setExtraPassword(@Nullable Password extraPassword) {
        this.extraPassword = extraPassword;

        database.setPassword(this.name(), extraPassword, false);
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
        //FUNCTIONALITY (audit, 2026-09-24): was updateExtraLoginTypeByName(extraLoginType) - persisted the
        //wrong, unchanged column, so a login-type change took effect in memory but was silently lost on
        //the next restart/reload.
        database.updateLoginTypeByName(this.name(), loginType);
    }

    public void setExtraLoginType(LoginType extraLoginType) {
        this.extraLoginType = extraLoginType;
        //FUNCTIONALITY (audit, 2026-09-24): was updateLoginTypeByName(loginType) - same swap as above, for
        //the extra-login-type column.
        database.updateExtraLoginTypeByName(this.name(), extraLoginType);
    }

    String name() {
        return this.data.getName();
    }

    @NotNull
    public AuthSetting getAuthSettings() {
        return authSettings;
    }

    public boolean hasProvenAuthAccess() {
        return hasProvenAuthAccess;
    }

    public boolean isDoubleVerificationEnabled() {
        return extraLoginType != null;
    }

    public boolean getIpAutoLogin() {
        return ipAutoLogin == null ? ConfigParams.playerIPAutoLogin : ipAutoLogin;
    }

    public Boolean getRawIpAutoLogin() {
        return this.ipAutoLogin;
    }

    @NotNull
    public Password getPassword() {
        return password;
    }

    @Nullable
    public Password getExtraPassword() {
        return extraPassword;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public LoginType getExtraLoginType() {
        return extraLoginType;
    }
}