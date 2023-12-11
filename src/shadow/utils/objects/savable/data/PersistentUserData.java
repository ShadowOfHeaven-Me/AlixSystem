package shadow.utils.objects.savable.data;

import org.bukkit.entity.Player;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.UserFileManager;
import alix.common.antibot.connection.filters.GeoIPTracker;
import alix.common.data.Password;
import alix.common.data.PasswordType;
import shadow.utils.objects.savable.multi.HomeList;

public final class PersistentUserData {

    public static final int CURRENT_DATA_LENGTH = 6;
    private final HomeList homes;
    private final Password password;
    private final String name;
    private PasswordType passwordType;
    private String ip;
    private long mutedUntil;

    //name - password - ip - homes
    private PersistentUserData(String[] splitData) {
        splitData = AlixUtils.ensureSplitDataCorrectness(splitData);
        this.name = splitData[0];
        this.password = Password.readFromSaved(splitData[1]);
        this.ip = splitData[2];
        this.homes = new HomeList(splitData[3]);
        this.mutedUntil = AlixUtils.parsePureLong(splitData[4]);
        this.passwordType = AlixUtils.readPasswordType(splitData[5]);
        //TODO: sixThArgument - is using pin & add /pin command, to toggle pin-only password
        addToMap();
    }

    private PersistentUserData(String name, String ip) {
        this.name = name;
        this.ip = ip;
        this.password = Password.newEmpty();
        this.homes = new HomeList();
        this.passwordType = AlixUtils.defaultPasswordType;
        //storageManager = null;
        addToMap();
    }

    private void addToMap() {
        UserFileManager.addData(this);
        GeoIPTracker.update(ip);
    }

    public static PersistentUserData from(String data) {
        return new PersistentUserData(AlixUtils.splitPersistentData(data));
    }

    public static PersistentUserData createDefault(String name, String ip) {
        return new PersistentUserData(name, ip);
    }

    public static PersistentUserData createFromPremiumPlayer(Player p) {
        PersistentUserData data = new PersistentUserData(p.getName(), p.getAddress().getAddress().getHostAddress());

        data.getPassword().setFrom(Password.createRandomUnhashed()); //ensure the account cannot be stolen in case the server suddenly ever switches to offline mode, and does not use FastLogin
        data.setPasswordType(PasswordType.PASSWORD);

        return data;
    }

    @Override
    public final String toString() {
        return name + "|" + password.toSavable() + "|" + ip + "|" + homes.toSavable() + "|" + mutedUntil + "|" + passwordType; //originalWorldUUID;
    }

    public final String getName() {
        return name;
    }

    public final String getHashedPassword() {
        return password.getHashedPassword();
    }

    public final Password getPassword() {
        return password;
    }

    public final String getSavedIP() {
        return ip;
    }

    public final HomeList getHomes() {
        return homes;
    }

    public final long getMutedUntil() {
        return mutedUntil;
    }

    public PasswordType getPasswordType() {
        return passwordType;
    }

    public void setPasswordType(PasswordType passwordType) {
        this.passwordType = passwordType;
    }

    public void setMutedUntil(long mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public void setPassword(String password) {
        this.password.setPassword(password);
    }

    public PersistentUserData setIP(String ip) {
        this.ip = ip;
        return this;
    }
}