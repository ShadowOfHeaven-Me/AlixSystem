package shadow.utils.objects.savable.data;

import alix.common.data.LoginType;
import alix.common.data.Password;
import org.bukkit.entity.Player;
import shadow.systems.login.filters.GeoIPTracker;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.objects.savable.multi.HomeList;

public final class PersistentUserData {

    public static final int CURRENT_DATA_LENGTH = 7;
    private final HomeList homes;
    private final String name;
    private final LoginParams loginParams;
    private String ip;
    private long mutedUntil;

    private PersistentUserData(String[] splitData) {
        splitData = AlixUtils.ensureSplitDataCorrectness(splitData);
        this.name = splitData[0];
        this.loginParams = new LoginParams(splitData[1]);
        this.ip = splitData[2];
        this.homes = new HomeList(splitData[3]);
        this.mutedUntil = AlixUtils.parsePureLong(splitData[4]);
        this.loginParams.initLoginTypes(splitData[5]);
        this.loginParams.initSettings(splitData[6]);
        UserFileManager.putData(this);
        GeoIPTracker.addIP(this.ip);//Add it here, as it was loaded
    }

    private PersistentUserData(String name, String ip, Password password) {
        this.name = name;
        this.ip = ip;
        this.loginParams = new LoginParams(password);
        this.homes = new HomeList();
        UserFileManager.putData(this);
        //The GeoIPTracker does not need to have any changes made
    }

    public static PersistentUserData from(String data) {
        return new PersistentUserData(AlixUtils.splitPersistentData(data));
    }

    public static PersistentUserData createDefault(String name, String ip, Password password) {
        return new PersistentUserData(name, ip, password);
    }

    public static PersistentUserData createFromPremiumPlayer(Player p) {
        return createFromPremiumInfo(p.getName(), p.getAddress().getAddress().getHostAddress());
    }

    public static PersistentUserData createFromPremiumInfo(String name, String ip) {
        //By creating a password we ensure the account cannot be stolen in case
        //the server suddenly ever switches to offline mode, and does not use FastLogin
        PersistentUserData data = new PersistentUserData(name, ip, Password.createRandom());
        data.setLoginType(LoginType.COMMAND);

        return data;
    }

    @Override
    public String toString() {
        return name + "|" + loginParams.passwordsToSavable() + "|" + ip + "|" + homes.toSavable() + "|" + mutedUntil + "|" + loginParams.settingsToSavable(); //originalWorldUUID;
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

    public String getSavedIP() {
        return ip;
    }

    public HomeList getHomes() {
        return homes;
    }

    public long getMutedUntil() {
        return mutedUntil;
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
        this.loginParams.setPassword(Password.empty());//
        this.loginParams.setExtraPassword(null);
        this.loginParams.setExtraLoginType(null);
    }

    public PersistentUserData setIP(String ip) {
        this.ip = ip;
        return this;
    }
}