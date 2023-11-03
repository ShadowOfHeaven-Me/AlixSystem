/*
package alix.common.data;

import alix.common.antibot.connection.types.GeoIPTracker;
import alix.common.utils.AlixCommonUtils;
import org.bukkit.entity.Player;

public abstract class AbstractPersistentUserData {

    public static final int CURRENT_DATA_LENGTH = 6;
    private final Password password;
    private final String name;
    //private StorageManager storageManager;
    private String ip;//, originalWorldUUID;
    private long mutedUntil;
    //private boolean usingPin;

    //name - password - ip - homes
    private AbstractPersistentUserData(String[] splitData) {
        splitData = AlixCommonUtils.ensureSplitDataCorrectness(splitData);
        this.name = splitData[0];
        this.password = Password.readFromSaved(splitData[1]); //Password.createAndDecrypt(splitData[1]);
        //usingPin = PinGui.recognizePin(password);
        this.ip = splitData[2];
        this.homes = new HomeList(splitData[3]);
        this.mutedUntil = AlixUtils.parsePureLong(splitData[4]);
        //this.passwordType = AlixUtils.readPasswordType(splitData[5]);
        //this.originalWorldUUID = JavaUtils.nullifyIfNullEquivalent(splitData[5]);
        //TODO: sixThArgument - is using pin & add /pin command, to toggle pin-only password
        //String sixThData = splitData[5];
        //storageManager = sixThData.equals("0") ? null : new StorageManager(sixThData); //TODO: FIX THIS
        addToMap();
    }

    private AbstractPersistentUserData(String name, String ip) {
        this.name = name;
        this.ip = ip;
        this.password = Password.newEmpty();
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

*/
/*    public boolean isEqualTo(PersistentUserData data) {
        return data.name.equals(name);
    }

    public boolean isNameEqualTo(String name) {
        return this.name.equals(name);
    }*//*


    @Override
    public final String toString() {
        //UserFileManager.setHasChanged(); <- should set to false instead (?) - no it should not
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

    public void setMutedUntil(long mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public void setPassword(String password) {
        this.password.setPassword(password);
    }

    public void setIP(String ip) {
        this.ip = ip;
    }
}*/
