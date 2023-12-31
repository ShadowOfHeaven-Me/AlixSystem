package shadow.utils.users.offline;

import alix.common.antibot.connection.filters.GeoIPTracker;
import alix.common.data.GuiType;
import alix.common.data.PasswordType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.systems.login.Verifications;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.objects.savable.data.gui.AlixGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.UserManager;
import shadow.utils.world.AlixWorld;

import java.util.concurrent.TimeUnit;

import static shadow.utils.main.AlixUtils.getVerificationReminderMessage;
import static shadow.utils.main.AlixUtils.requireCaptchaVerification;

public final class UnverifiedUser {

    //private static final String registerSuccess = Messages.get("register-success");
    private final Player player;
    private final PersistentUserData data; //<- Can be null (other variables may be null as well, but this one is decently important)
    private final Captcha captcha;
    private final PacketBlocker blocker;
    //private final Location currentLocation;
    private final GameMode originalGameMode;
    private final String ipAddress, joinMessage;
    private final boolean isGuiUser, captchaInitialized, originalCollidableState;
    private final GuiType passwordGuiType;
    private AlixGui alixGui;
    private String verificationMessage;
    private boolean hasCompletedCaptcha, isGUIInitialized;

    public UnverifiedUser(Player player, PersistentUserData data, String ipAddress, String joinMessage) {
        this.player = player;
        this.data = data;
        this.ipAddress = ipAddress;
        this.joinMessage = joinMessage;

        this.originalCollidableState = player.isCollidable();
        this.player.setCollidable(false); // <- excluding the player from the collision calculation to save a bit of cpu & to stop entity collision at verification

        boolean hasAccount = hasAccount();//data != null
        boolean registered = isRegistered(hasAccount);

        this.hasCompletedCaptcha = hasAccount || !requireCaptchaVerification;//has an account or captcha is disabled
        this.captchaInitialized = !hasCompletedCaptcha;//the captcha was initialized if the user is required to complete it

        if (hasAccount)
            this.passwordGuiType = data.getPasswordType() == PasswordType.PIN ? GuiType.PIN : AlixUtils.anvilPasswordGui ? GuiType.ANVIL : null;
        else
            this.passwordGuiType = AlixUtils.anvilPasswordGui ? GuiType.ANVIL : AlixUtils.defaultPasswordType == PasswordType.PIN ? GuiType.PIN : null;

        this.isGuiUser = this.passwordGuiType != null;
        //this.isGuiUser = data != null ? data.getPasswordType() == PasswordType.PIN || AlixUtils.anvilPasswordGui : AlixUtils.anvilPasswordGui || AlixUtils.defaultPasswordType == PasswordType.PIN;

        this.originalGameMode = player.getGameMode();
        this.player.setGameMode(GameMode.ADVENTURE); //Set the gamemode to adventure (on spectator the map is invisible and on creative the countdown is - also it prevents a possible accidental arm animation packet spam)

        if (player.isDead()) player.spigot().respawn();

        if (this.captchaInitialized) {
            this.captcha = Captcha.nextCaptcha(this); //Fast Captcha get
            this.player.setPersistent(false); //Do not save the possibly bot player
        } else this.captcha = null;


        if (isGuiUser && hasCompletedCaptcha)
            this.alixGui = PasswordGui.newBuilder(this, this.passwordGuiType);
        else {
            this.verificationMessage = getVerificationReminderMessage(registered);
            if (!AlixUtils.repeatedVerificationReminderMessages) player.sendRawMessage(this.verificationMessage);
        }

        this.blocker = PacketBlocker.getPacketBlocker(this, this.passwordGuiType);
        this.openPasswordBuilderGUI();

/*        AlixScheduler.runLaterAsync(this::spoofPackets, 750, TimeUnit.MILLISECONDS);
        AlixScheduler.runLaterAsync(this::spoofPackets, 1500, TimeUnit.MILLISECONDS);
        AlixScheduler.runLaterAsync(this::spoofPackets, 2250, TimeUnit.MILLISECONDS);*/
    }

    public void spoofVerificationPackets() {
        ReflectionUtils.sendLoginEffectPacket(this.player.getEntityId(), this.blocker.getChannel());
        if (captchaInitialized) this.captcha.sendPackets();
    }

    public void uninject(boolean leave) {//removes all that was ever assigned and related (but does not teleport back)
        if (captchaInitialized) captcha.uninject();
        else if (isGUIInitialized && !leave) player.closeInventory();

        if (!hasCompletedCaptcha) return;//do not return the original params as captcha unverified are non-persistent

        this.player.setCollidable(this.originalCollidableState); // <- returning the player to his original collide state
        this.player.setGameMode(this.originalGameMode);
    }

    public void disableActionBlockerAndUninject() {
        this.blocker.stop();
        this.uninject(false);
    }

    @NotNull
    public final Player getPlayer() {
        return player;
    }

    @NotNull
    public final PacketBlocker getPacketBlocker() {
        return blocker;
    }

    @Nullable
    public final PersistentUserData getData() {
        return data;
    }

    @NotNull
    public final String getIPAddress() {
        return ipAddress;
    }

    @NotNull
    public final Location getCurrentLocation() {
        return AlixWorld.TELEPORT_LOCATION;//The current location should be the captcha world (set by the OfflineExecutors)
    }

    public final boolean isCaptchaInitialized() {
        return captchaInitialized;
    }

    public final boolean isCaptchaCorrect(String s) {
        return this.captchaInitialized && this.captcha.isCorrect(s);
    }

    @Nullable
    public final String getVerificationMessage() {
        return verificationMessage;
    }

    @Nullable
    public final AlixGui getPasswordBuilder() {
        return alixGui;
    }

    public final boolean isGuiUser() {
        return isGuiUser;
    }

    /*    public final World getOriginalWorld() {
        return originalWorld;
    }

    public final Location getOriginalLocation() {
        return originalLocation;
    }*/

/*    public void autoLogin() {
        uninject();
        logIn();
    }*/

    public final void completeCaptcha() {
        //if (this.hasCompletedCaptcha) return;
        this.verificationMessage = Messages.unregisteredUserMessage;
        if (!AlixUtils.repeatedVerificationReminderMessages) this.player.sendRawMessage(this.verificationMessage);

        this.blocker.startLoginKickTask();

        this.hasCompletedCaptcha = true;

        if (this.isGuiUser) {//synchronize, since we invoke it async on 1.17+
            if (PacketBlocker.serverboundNameVersion) AlixScheduler.sync(this::initGUI0);
            else this.initGUI0();
        }
    }

    private void initGUI0() {
        this.alixGui = PasswordGui.newBuilder(this, this.passwordGuiType);
        this.blocker.updateBuilder();
        this.openPasswordBuilderGUI();
    }

/*    public Object getMapPacket() {
        return ((MapCaptcha) captcha).
    }*/

    public final boolean isGUIInitialized() {
        return isGUIInitialized;
    }

    public final void setGUIInitialized(boolean initialized) {
        this.isGUIInitialized = initialized;
    }

    public final void openPasswordBuilderGUI() {
        if (this.alixGui != null) this.player.openInventory(this.alixGui.getGUI());
    }

    public final boolean hasCompletedCaptcha() {
        return hasCompletedCaptcha;
    }

    public void logInSync() {//invoked sync
        this.logIn0();
        this.logIn1();
    }

    public void logInAsync() {//invoked async
        this.logIn0();
        AlixScheduler.sync(this::logIn1);
    }

    private void logIn1() {
        //this.player.setGameMode(this.originalGameMode);
        OriginalLocationsManager.teleportBack(player, true);//tp back 'n reset the illusion packet blocking made
        AlixUtils.loginCommandList.invoke(player);
    }

    private void logIn0() {//the common part
        this.removeVerificationBecauseVerified();
        UserManager.addOfflineUser(player, data, ipAddress, this.blocker.getChannel());
        AlixScheduler.async(() -> ReflectionUtils.resetLoginEffectPackets(this));
        //AlixScheduler.runLaterAsync(() -> ReflectionUtils.resetLoginEffectPackets(this), 1, TimeUnit.SECONDS);
    }

    public void registerSync(String password) {//invoked sync
        this.register0(password);
        this.register1();
    }

    public void registerAsync(String password) {//invoked async
        this.register0(password);
        AlixScheduler.sync(this::register1);
    }

    private void register1() {
        //this.player.setGameMode(this.originalGameMode);
        OriginalLocationsManager.teleportBack(player, true);
        AlixUtils.registerCommandList.invoke(player);
    }

    private void register0(String password) {//the common part
        PersistentUserData data = UserManager.register(this.player, password, this.ipAddress, this.blocker.getChannel());
        data.setPasswordType(isGuiUser ? alixGui.getType().toPasswordType() : PasswordType.PASSWORD);
        this.removeVerificationBecauseVerified();

        if (captchaInitialized) {
            GeoIPTracker.removeTempIP(this.ipAddress);
            this.player.setPersistent(true); //Start saving the player which was verified by captcha
            //Only set the original gamemode on registration, as non-persistent player's information are not saved
            AlixScheduler.runLaterAsync(() -> {
                ReflectionUtils.resetLoginEffectPackets(this);
                if (joinMessage != null)
                    AlixUtils.broadcastFast0(this.joinMessage);//only send the join message if it wasn't removed by someone else
            }, 1, TimeUnit.SECONDS);
        } else {
            AlixScheduler.runLaterAsync(() -> ReflectionUtils.resetLoginEffectPackets(this), 1, TimeUnit.SECONDS);
        }
    }

    private void removeVerificationBecauseVerified() {
        Verifications.remove(player);
        this.disableActionBlockerAndUninject();
    }

    public boolean isPasswordCorrect(String s) {
        return data.getPassword().isCorrect(s);
    }

    public boolean hasAccount() {
        return data != null;
    }

    public boolean isRegistered() {
        return isRegistered(hasAccount());
    }

    private boolean isRegistered(boolean hasAccount) {
        return hasAccount && data.getPassword().isSet();
    }
}