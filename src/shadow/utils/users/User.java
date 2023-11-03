package shadow.utils.users;

import org.bukkit.entity.Player;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.objects.savable.multi.HomeList;

import java.util.UUID;

public class User {// implements ObjectSerializable {

    private final Player player;
    protected final UUID uuid;
    private final PersistentUserData data;
    private final short maxHomes;
    private long nextPossibleChatTime;
    private final boolean canBypassChatStatus, canSendColoredMessages;
    private boolean canReceiveTeleportRequests;
    /*    protected boolean isOwner;*/

    //name - password - ip - homes
    protected User(Player player, PersistentUserData data) {
        this.player = player;
        this.data = data;
        this.uuid = player.getUniqueId();
        if (player.isOp()) {
            maxHomes = 32767;
            canBypassChatStatus = canSendColoredMessages = true;
            canReceiveTeleportRequests = true;
            //canReceiveTeleportRequests = false;
            return;
        }
        canBypassChatStatus = AlixUtils.hasChatBypass(player);
        canSendColoredMessages = AlixUtils.canSendColoredChatMessages(player);
        maxHomes = AlixUtils.getMaxHomes(player);
        canReceiveTeleportRequests = true;
    }

    protected User(Player player) {
        this(player, UserFileManager.getOrCreatePremiumInformation(player));
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return data.getName();
    }

    public final boolean canBypassChatStatus() {
        return canBypassChatStatus;
    }

    public final boolean canSendColoredMessages() {
        return canSendColoredMessages;
    }

    @Override
    public boolean equals(Object o) {
        //if (o instanceof Player) return ((Player) o).getUniqueId().equals(uuid);
        return o instanceof User && ((User) o).uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

/*    public boolean hasUUID(UUID uuid) {
        return this.uuid.equals(uuid);
    }*/

    public void changePassword(String password) {
        data.setPassword(password);
        //saveData();
    }

/*    protected int getNumericOwner() {
        return 0; //isOwner ? 1 : 0;
    }*/

/*    public PersistentUserData getPersistentData() {
        return data; //PersistentUserData.of(name + "|" + getPassword() + "|" + data.getSavedIP() + "|" + Home.getSavableHomes(getHomes()));
    }*/

    //name - password - ip - homes
/*    private void saveData() {
        //UserFileManager.add(this);
*//*        if (getDataContainer().has(JavaUtils.saveKey, PersistentDataType.STRING) && password.equals("none")) return;
        getDataContainer().set(JavaUtils.saveKey, PersistentDataType.STRING, Password.encryptPassword(password, passwordEncrypt) +
                "þ" + currentIP + "þ"+ getNumericOwner() +"þ" + passwordEncrypt + "þ" + getSavableHomes());*//*
    }*/

/*    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }*/

/*    public void sendReminder() {
        p.sendMessage(JavaUtils.getLoginReminderMessage(isRegistered()));
        //JavaScheduler.runLaterAsync(() -> p.sendMessage(JavaUtils.getLoginReminderMessage(isRegistered())), 1L, TimeUnit.SECONDS);
    }*/

    public short getMaxHomes() {
        return maxHomes;
    }

    public HomeList getHomes() {
        return data.getHomes();
    }

    public boolean canReceiveTeleportRequests() {
        return canReceiveTeleportRequests;
    }

    public void setCanReceiveTeleportRequests(boolean canReceiveTeleportRequests) {
        this.canReceiveTeleportRequests = canReceiveTeleportRequests;
    }

    public long getNextPossibleChatTime() {
        return nextPossibleChatTime;
    }

    public void setNextPossibleChatTime(long nextPossibleChatTime) {
        this.nextPossibleChatTime = nextPossibleChatTime;
    }

    /*    public void setMuted(long till) {
        data.setMutedUntil(till);
    }*/

    public boolean isMuted() {
        return isMuted(System.currentTimeMillis());
    }

    public boolean isMuted(long now) {
        return now < data.getMutedUntil();
    }

/*    private String getSavableHomes() {
        if (homes.isEmpty()) return "0";
        StringBuilder a = new StringBuilder();
        for (Home b : homes) a.append(b.toSavable()).append(";");
        return a.substring(0, a.length() - 1);
    }*/

/*
    public void setVanished(boolean vanish) {
        if (this.vanish = vanish) {
            p.hidePlayer(Main.instance, p);
            p.addPotionEffect(JavaUtils.vanishInvisibilityEffect);
            p.spigot().setCollidesWithEntities(false);
            //UserManager.notVanishedUserNicknames.remove(p.getName());
        } else {
            p.showPlayer(Main.instance, p);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            p.spigot().setCollidesWithEntities(true);
            //UserManager.notVanishedUserNicknames.add(p.getName());
        }
    }*/

/*    private void updateHomes(String a) {
        if (a.equals("0")) return;
        String[] b = JavaUtils.split(a, ';');
        for (String c : b) {
            Home d = Home.fromString(c);
            if (d != null) homes.add(d);
        }
    }*/

    public void sendMessage(String message) {
        AlixUtils.sendMessage(player, message);
    }

/*    @Override
    public String getId() {
        return uuid.toString();
    }

    @Override
    public String getCollectionTable() {
        return "users";
    }

    @Override
    public void onSerialize(Map<String, Object> map) {
        map.put("name",
    }

    @Override
    public void onDeserialize(Map<String, Object> map) {

    }*/
}
