package shadow.utils.objects.savable.data;

import alix.common.data.LoginType;
import alix.common.data.Password;
import alix.common.data.security.Hashing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.utils.main.AlixUtils;

public final class LoginParams {

    private Password password, extraPassword;
    private LoginType loginType, extraLoginType;
    private Boolean ipAutoLogin;
    private Byte defaultingPasswordHash;

    LoginParams(String line) {
        String[] a = line.split(";");
        this.password = Password.readFromSaved(a[0]);
        if (a.length >= 2) {
            this.extraPassword = Password.readFromSaved(a[1]);
            if (a.length == 3) defaultingPasswordHash = Byte.parseByte(a[2]);
        }
    }

    LoginParams(Password password) {
        this.password = password;
        this.loginType = AlixUtils.defaultLoginType;
    }

    void initLoginTypes(String data) {
        String[] a = data.split(";");
        this.loginType = AlixUtils.readLoginType(a[0], AlixUtils.defaultLoginType);
        if (a.length == 2) this.extraLoginType = AlixUtils.readLoginType(a[1], null);
    }

    void initSettings(String settings) {
        this.ipAutoLogin = settings.equals("0") || settings.equals("null") ? null : Boolean.parseBoolean(settings);
    }

    public String passwordsToSavable() {
        if (extraPassword == null) return password.toSavable();
        return password.toSavable() + ";" + extraPassword.toSavable();
    }

    public String settingsToSavable() {
        if (extraLoginType == null) return loginType + "|" + ipAutoLogin;
        return loginType + ";" + extraLoginType + "|" + ipAutoLogin;
    }

    public boolean isDoubleVerificationEnabled() {
        return extraLoginType != null;
    }

    public boolean getIpAutoLogin() {
        return ipAutoLogin == null ? AlixUtils.playerIPAutoLogin : ipAutoLogin;//default to config it not set manually by the user
    }

    public void setIpAutoLogin(boolean ipAutoLogin) {
        this.ipAutoLogin = ipAutoLogin;
    }

    public byte getDefaultingPasswordHash() {
        return defaultingPasswordHash == null ? Hashing.CONFIG_HASH_ID : defaultingPasswordHash;
    }

    public void setDefaultingPasswordHash(byte defaultingPasswordHash) {
        this.defaultingPasswordHash = defaultingPasswordHash;
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

    public LoginType getLoginType() {
        return loginType;
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }

    public LoginType getExtraLoginType() {
        return extraLoginType;
    }

    public void setExtraLoginType(LoginType extraLoginType) {
        this.extraLoginType = extraLoginType;
    }
}