package alix.common.data;

import alix.common.antibot.IPUtils;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.file.UserFileManager;
import alix.common.data.loc.AlixLocationList;
import alix.common.data.loc.provider.LocationListProvider;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Arrays;

public final class PersistentUserData {

    private static final LocationListProvider homesProvider = LocationListProvider.createImpl();
    private static final int CURRENT_DATA_LENGTH = 9;
    private final AlixLocationList homes;
    private final String name;
    private final LoginParams loginParams;
    private @NotNull InetAddress ip;
    private long mutedUntil, lastSuccessfulLogin;

    //name | password1 ; password2 | ip | homes | mutedUntil | login type1 ; login type2 | login settings
    private PersistentUserData(String[] splitData) {
        //splitData = ensureSplitDataCorrectness(splitData);
        this.name = splitData[0];
        this.loginParams = new LoginParams(splitData[1]);
        this.ip = IPUtils.fromAddress(splitData[2]);
        this.homes = homesProvider.fromSavable(splitData[3]);
        this.mutedUntil = Long.parseLong(splitData[4]);
        this.loginParams.initLoginTypes(splitData[5]);
        this.loginParams.initSettings(splitData[6]);
        this.loginParams.initAuthSettings(splitData[7]);
        this.lastSuccessfulLogin = Long.parseLong(splitData[8]);
        UserFileManager.putData(this);
        GeoIPTracker.addIP(this.ip);//Add it here, as it was loaded
    }

    private PersistentUserData(String name, InetAddress ip, Password password) {
        this.name = name;
        this.ip = ip;
        this.loginParams = new LoginParams(password);
        this.homes = homesProvider.newList();
        UserFileManager.putData(this);
        //The GeoIPTracker add should not be invoked
    }

    public static PersistentUserData from(String data) {
        return new PersistentUserData(ensureSplitDataCorrectness(splitPersistentData(data)));
    }

    public static PersistentUserData createDefault(String name, InetAddress ip, Password password) {
        return new PersistentUserData(name, ip, password);
    }

    public static PersistentUserData createFromPremiumInfo(String name, InetAddress ip) {
        return createFromPremiumInfo(name, ip, Password.createRandom());
    }

    public static PersistentUserData createFromPremiumInfo(String name, InetAddress ip, Password password) {
        //By creating a password we ensure the account cannot be stolen in case
        //the server suddenly ever switches to offline mode, and does not use FastLogin
        PersistentUserData data = new PersistentUserData(name, ip, password);
        data.setLoginType(LoginType.COMMAND);

        return data;
    }

    private static String[] ensureSplitDataCorrectness(String[] splitData) {
        int correctLength = PersistentUserData.CURRENT_DATA_LENGTH;
        int splitDataLength = splitData.length;
        int diff = correctLength - splitDataLength;

        if (diff == 0) return splitData;

        String[] correctData = new String[correctLength];

        System.arraycopy(splitData, 0, correctData, 0, splitDataLength);

        Arrays.fill(correctData, splitDataLength, correctLength, "0");

        return correctData;
    }

    private static String[] splitPersistentData(String data) {
        return data.split("\\|");
    }

    @Override
    public String toString() {
        return name + "|" + loginParams.passwordsToSavable() + "|" + ip.getHostAddress() + "|" + homes.toSavable() + "|" + mutedUntil + "|"
                + loginParams.settingsToSavable() + "|" + loginParams.authSettingsToSavable() + "|" + lastSuccessfulLogin; //originalWorldUUID;
    }

    public String getName() {
        return name;
    }

    public Password getPassword() {
        return loginParams.getPassword();
    }

    public LoginParams getLoginParams() {
        return loginParams;
    }

    public InetAddress getSavedIP() {
        return ip;
    }

    public AlixLocationList getHomes() {
        return homes;
    }

    public long getMutedUntil() {
        return mutedUntil;
    }

    public long getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void updateLastSuccessfulLoginTime() {
        this.lastSuccessfulLogin = System.currentTimeMillis();
    }

    public LoginType getLoginType() {
        return this.loginParams.getLoginType();
    }

    public void setLoginType(LoginType loginType) {
        this.loginParams.setLoginType(loginType);
    }

    public void setMutedUntil(long mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public void setPassword(String password) {
        this.loginParams.setPassword(Password.fromUnhashed(password));
    }

    public void setPassword(Password password) {
        this.loginParams.setPassword(password);
    }

    public void resetPasswords() {
        this.loginParams.setPassword(Password.empty());
        this.loginParams.setExtraPassword(null);
        this.loginParams.setExtraLoginType(null);
        this.loginParams.setAuthSettings(AuthSetting.PASSWORD);
        this.loginParams.setHasProvenAuthAccess(false);
    }

    public PersistentUserData setIP(InetAddress ip) {
        this.ip = ip;
        return this;
    }
}