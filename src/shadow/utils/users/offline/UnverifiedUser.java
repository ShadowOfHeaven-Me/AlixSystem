package shadow.utils.users.offline;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.systems.gui.impl.IpAutoLoginGUI;
import shadow.systems.login.LoginVerification;
import shadow.systems.login.Verifications;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.filters.GeoIPTracker;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.packet.types.verified.SelfDelegatingProcessor;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.UserManager;
import shadow.utils.world.AlixWorld;

import java.util.concurrent.TimeUnit;

import static shadow.utils.main.AlixUtils.getVerificationReminderMessagePacket;
import static shadow.utils.main.AlixUtils.requireCaptchaVerification;

public final class UnverifiedUser {

    //private static final String registerSuccess = Messages.get("register-success");
    private static final Object loginSuccessMessagePacket = OutMessagePacketConstructor.construct(Messages.getWithPrefix("login-success"));
    private final Player player;
    private final PersistentUserData data; //<- Can be null (other variables may be null as well, but this one is decently important)
    private final Captcha captcha;
    private final PacketInterceptor duplexHandler;
    private PacketBlocker blocker;
    //private final Location currentLocation;
    //private final GameMode originalGameMode;
    private final String ipAddress, joinMessage;
    private final boolean captchaInitialized, originalCollidableState;
    private LoginType loginType;
    private LoginVerification loginVerification;
    private AlixVerificationGui alixGui;
    private Object verificationMessagePacket;
    public int loginAttempts, captchaAttempts;
    private boolean hasCompletedCaptcha, isGUIInitialized, isGuiUser;

    public UnverifiedUser(Player player, LoginInfo info, String joinMessage) {
        this.player = player;
        this.data = info.getData();
        this.ipAddress = info.getIP();
        this.duplexHandler = info.getPacketInterceptor();
        this.joinMessage = joinMessage;

        this.originalCollidableState = player.isCollidable();
        this.player.setCollidable(false); // <- excluding the player from the collision calculation to save a bit of cpu & to stop entity collision at verification

        boolean hasAccount = hasAccount();//data != null
        boolean registered = isRegistered();

        this.hasCompletedCaptcha = hasAccount || !requireCaptchaVerification;//has an account or captcha is disabled
        this.captchaInitialized = !hasCompletedCaptcha;//the captcha was initialized if the user is required to complete it

        if (hasAccount)
            this.loginType = data.getLoginType(); //data.getLoginType() == LoginType.PIN ? LoginType.PIN : AlixUtils.anvilPasswordGui ? LoginType.ANVIL : LoginType.COMMAND;
        else
            this.loginType = AlixUtils.anvilPasswordGui ? LoginType.ANVIL : AlixUtils.pinPasswordGui ? LoginType.PIN : LoginType.COMMAND;

        this.isGuiUser = this.loginType != LoginType.COMMAND;

        //new PacketInterceptor(this.player);//the delegator to the processor
        //this.isGuiUser = data != null ? data.getPasswordType() == PasswordType.PIN || AlixUtils.anvilPasswordGui : AlixUtils.anvilPasswordGui || AlixUtils.defaultPasswordType == PasswordType.PIN;

        //this.originalGameMode = player.getGameMode();
        //this.player.setGameMode(GameMode.ADVENTURE); //Set the gamemode to adventure (on spectator the map is invisible and on creative the countdown is - also it prevents a possible accidental arm animation packet spam)

        if (player.isDead()) player.spigot().respawn();

        if (this.captchaInitialized) {
            this.captcha = Captcha.nextCaptcha(this); //Fast Captcha get
            this.player.setPersistent(false); //Do not save the possibly bot player
        } else this.captcha = null;


        if (isGuiUser && hasCompletedCaptcha)
            this.alixGui = PasswordGui.newBuilder(this, this.loginType);
        else {
            this.verificationMessagePacket = getVerificationReminderMessagePacket(registered, hasAccount);
            this.writeAndFlushSilently(this.verificationMessagePacket);
        }

        if (registered) this.loginVerification = new LoginVerification(data.getPassword(), true);

        this.blocker = PacketBlocker.getPacketBlocker(this, this.loginType, this.duplexHandler);//the actual processor
        this.duplexHandler.setProcessor(this.blocker);//setting the processor
        //this.openPasswordBuilderGUI();
        //if (!captchaInitialized) this.spoofVerificationPackets();//spoof the verification packets immediately
    }

    public final void writeSilently(Object messagePacket) {
        this.duplexHandler.writeSilently(messagePacket);
    }

    public final void writeAndFlushSilently(Object messagePacket) {
        this.duplexHandler.writeAndFlushSilently(messagePacket);
    }

    public void spoofVerificationPackets() {
        ReflectionUtils.sendLoginEffectPacket(this);
        if (isGUIInitialized) AlixScheduler.sync(this::openPasswordBuilderGUI);
        if (captchaInitialized) this.captcha.sendPackets();
    }

    public final void uninject() {//removes all that was ever assigned and related (but does not teleport back)
        if (captchaInitialized) captcha.uninject();

        if (!hasCompletedCaptcha) return;//do not return the original params as captcha unverified are non-persistent

        this.player.setCollidable(this.originalCollidableState); // <- returning the player to his original collide state
    }

    //the action blocker is now automatically disabled by
    //overriding the PacketProcessor in the User constructor
    public final void disableActionBlockerAndUninject() {
        this.duplexHandler.setProcessor(new SelfDelegatingProcessor(this.duplexHandler));
        this.uninject();
    }

    @NotNull
    public final Player getPlayer() {
        return player;
    }

    @NotNull
    public final PacketBlocker getPacketBlocker() {
        return blocker;
    }

    @NotNull
    public PacketInterceptor getDuplexHandler() {
        return duplexHandler;
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
    public final Object getVerificationMessagePacket() {
        return verificationMessagePacket;
    }

    @Nullable
    public final AlixVerificationGui getPasswordBuilder() {
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
        this.verificationMessagePacket = AlixUtils.unregisteredUserMessagePacket;
        this.writeAndFlushSilently(this.verificationMessagePacket);

        this.blocker.startLoginKickTask();

        this.hasCompletedCaptcha = true;

        if (this.isGuiUser) {//synchronize, since we invoke it async on 1.17+
            if (PacketBlocker.serverboundNameVersion) AlixScheduler.sync(this::initGUI0);
            else this.initGUI0();
        }
    }

    private void initGUI0() {
        this.player.closeInventory();
        this.alixGui = PasswordGui.newBuilder(this, this.loginType);
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
        if (isGUIInitialized) this.player.openInventory(this.alixGui.getGUI());
    }

    public final boolean hasCompletedCaptcha() {
        return hasCompletedCaptcha;
    }

    private boolean initDoubleVer() {
        if (data.getLoginParams().isDoubleVerificationEnabled() && loginVerification.isPhase1()) {
            this.loginType = data.getLoginParams().getExtraLoginType();
            this.isGuiUser = loginType != LoginType.COMMAND;

            this.blocker = PacketBlocker.getPacketBlocker(this, this.loginType, this.duplexHandler);
            this.duplexHandler.setProcessor(this.blocker);

            this.isGUIInitialized = false;
            if (isGuiUser) AlixScheduler.sync(this::initGUI0);

            this.loginVerification = new LoginVerification(data.getLoginParams().getExtraPassword(), false);
            return true;
        }
        return false;
    }

    public void logInSync() {//invoked sync
        if (initDoubleVer()) return;
        this.logIn1();
        AlixScheduler.async(this::logIn0);
    }

    public void logInAsync() {//invoked async
        if (initDoubleVer()) return;
        AlixScheduler.sync(this::logIn1);
        this.logIn0();
    }

    private void logIn1() {
        //this.player.setGameMode(this.originalGameMode);
        OriginalLocationsManager.teleportBack(player, true);//tp back 'n reset the illusion packet blocking made
        AlixUtils.loginCommandList.invoke(player);
        //this.player.setGameMode(this.originalGameMode);
    }

    private void logIn0() {//the common part
        this.writeAndFlushSilently(loginSuccessMessagePacket);//invoked here, since the
        this.removeVerificationBecauseVerified();
        UserManager.addOfflineUser(player, data, ipAddress, duplexHandler);
        //AlixScheduler.async(() -> ReflectionUtils.resetLoginEffectPackets(this));
        ReflectionUtils.resetLoginEffectPackets(this);//no need to execute this async here, since it's this method code is executed async by the AlixScheduler
        //AlixScheduler.runLaterAsync(() -> ReflectionUtils.resetLoginEffectPackets(this), 1, TimeUnit.SECONDS);
    }

    public void registerSync(String password) {//invoked sync
        this.register1();
        this.register0(password);
    }

    public void registerAsync(String password) {//invoked async
        AlixScheduler.sync(this::register1);
        this.register0(password);
    }

    private void register1() {
        //this.player.setGameMode(this.originalGameMode);
        OriginalLocationsManager.teleportBack(player, true);
        AlixUtils.registerCommandList.invoke(player);
        if (captchaInitialized) GeoIPTracker.removeTempIP(this.ipAddress);
        if (!hasAccount()) IpAutoLoginGUI.add(this.player);
        //this.player.setGameMode(this.originalGameMode);
    }

    private void register0(String password) {//the common part
        //Since this code is executed on the Netty threads (or on the main thread on pre-1.17),
        //and the password hashing algorithm could take a millisecond or two,
        //we will execute the code async (already is, earlier in the code)
        //AlixScheduler.async(() -> {
        PersistentUserData data = UserManager.register(this.player, password, this.ipAddress, this.duplexHandler);
        data.setLoginType(this.loginType);
        this.removeVerificationBecauseVerified();

        if (captchaInitialized) {
            this.player.setPersistent(true); //Start saving the player which was verified by captcha
            AlixScheduler.runLaterAsync(() -> {
                ReflectionUtils.resetLoginEffectPackets(this);
                if (joinMessage != null)
                    AlixUtils.broadcastFast0(this.joinMessage);//only send the join message if it wasn't removed by someone else
            }, 1, TimeUnit.SECONDS);
        } else
            AlixScheduler.runLaterAsync(() -> ReflectionUtils.resetLoginEffectPackets(this), 1, TimeUnit.SECONDS);
        //});
    }

    private void removeVerificationBecauseVerified() {
        Verifications.remove(player);
        this.disableActionBlockerAndUninject();
        AlixDeque.forEach(this.duplexHandler::writeSilently, this.blocker.getBlockedChatPackets());
        this.duplexHandler.flushSilently();
    }

    public boolean isPasswordCorrect(String s) {
        return this.loginVerification.isPasswordCorrect(s); //data.getPassword().isEqualTo(s);
    }

    public boolean hasAccount() {
        return data != null;
    }

    public boolean isRegistered() {
        return hasAccount() && data.getPassword().isSet();
    }
}