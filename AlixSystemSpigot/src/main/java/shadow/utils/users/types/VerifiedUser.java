package shadow.utils.users.types;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.loc.impl.bukkit.BukkitHomeList;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.NettyUtils;
import shadow.utils.objects.packet.types.verified.VerifiedPacketProcessor;

import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class VerifiedUser extends AbstractAlixCtxUser {// implements ObjectSerializable {

    private final Player player;
    private final PersistentUserData data;
    private final UUID uuid;
    private final User retrooperUser;
    private final VerifiedPacketProcessor duplexProcessor;
    private final short maxHomes;
    //private final ByteBufHarvester bufHarvester;
    private final boolean canBypassChatStatus, canSendColoredMessages;
    private long nextPossibleChatTime;
    private boolean canReceiveTeleportRequests;
    public final AtomicReference<Location> originalLocation = new AtomicReference<>();
    /*    protected boolean isOwner;*/


    public VerifiedUser(Player player, PersistentUserData data, User retrooperUser, ChannelHandlerContext silentContext, Consumer<VerifiedUser> onFirstPlayPacket) {
        super(silentContext);
        Objects.requireNonNull(data);//require the data to be non-null
        //Main.logError("VERIFIED MADE");
        this.player = player;
        this.data = data;
        //this.data.updateLastSuccessfulLoginTime();
        this.retrooperUser = retrooperUser;
        this.uuid = player.getUniqueId();
        //this.bufHarvester = ByteBufHarvester.harvesterOf(this.getChannel());
        this.duplexProcessor = VerifiedPacketProcessor.getProcessor(this, onFirstPlayPacket);

        if (player.isOp()) {
            this.maxHomes = 32767;
            this.canBypassChatStatus = canSendColoredMessages = true;
            this.canReceiveTeleportRequests = true;
            //canReceiveTeleportRequests = false;
            return;
        }
        this.canBypassChatStatus = AlixUtils.hasChatBypass(player);
        this.canSendColoredMessages = AlixUtils.canSendColoredChatMessages(player);
        this.maxHomes = AlixUtils.getMaxHomes(player);
        this.canReceiveTeleportRequests = true;
    }

    public VerifiedUser(Player player, TemporaryUser user, Consumer<VerifiedUser> onFirstPlayPacket) {
        this(player, user.getLoginInfo().getData().setIP(player.getAddress().getAddress()), user.retrooperUser(), NettyUtils.getSilentContext(user.getChannel()), onFirstPlayPacket);
    }

    /*private VerifiedUser(Player p, User user) {
        this(p, UserFileManager.getOrCreatePremiumInformation(p.getName(), p.getAddress().getAddress()), user, NettyUtils.getSilentContext((Channel) user.getChannel()));
    }*/

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public String getName() {
        return data.getName();
    }

    public UUID getUUID() {
        return uuid;
    }

    public PersistentUserData getData() {
        return data;
    }

    public VerifiedPacketProcessor getDuplexProcessor() {
        return duplexProcessor;
    }

    public void onQuit() {
        Location orgLoc = originalLocation.get();
        if (orgLoc != null) this.player.teleport(orgLoc);

        this.duplexProcessor.onQuit();
    }

    /*    public PersistentUserData getData() {
        return data;
    }*/

    public boolean canBypassChatStatus() {
        return canBypassChatStatus;
    }

    public boolean canSendColoredMessages() {
        return canSendColoredMessages;
    }

    @Override
    public boolean equals(Object o) {
        return o == this; //o != null && o.getClass() == User.class && ((User) o).uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

/*    public boolean hasUUID(UUID uuid) {
        return this.uuid.equals(uuid);
    }*/

    public void changePassword(String password) {
        this.data.setPassword(password);
        this.data.setLoginType(LoginType.COMMAND);
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

    public BukkitHomeList getHomes() {
        return (BukkitHomeList) data.getHomes();
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
        return wasMutedAt(System.currentTimeMillis());
    }

    public boolean wasMutedAt(long time) {
        return time < data.getMutedUntil();
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

/*    public void quit() {
        //if (duplexHandler != null) this.duplexHandler.stop();
    }*/

    public InetAddress getIPAddress() {
        return data.getSavedIP();
    }

    @Override
    public User retrooperUser() {
        return retrooperUser;
    }

    @Override
    public VerifiedPacketProcessor getPacketProcessor() {
        return duplexProcessor;
    }

    @Override
    public boolean isVerified() {
        return true;
    }

    /*@Override
    public ByteBufHarvester bufHarvester() {
        return this.bufHarvester;
    }*/

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
