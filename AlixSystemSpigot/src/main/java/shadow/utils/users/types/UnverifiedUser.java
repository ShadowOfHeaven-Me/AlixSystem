package shadow.utils.users.types;

import alix.common.data.AuthSetting;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.login.LoginVerification;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.api.event.types.AuthReason;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.gui.impl.IpAutoLoginGUI;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.systems.login.reminder.message.VerificationMessage;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.api.AlixEventInvoker;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.misc.CommandsPacketManager;
import shadow.utils.misc.effect.PotionEffectHandler;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.ByteBufHarvester;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.UserManager;
import ua.nanit.limbo.integration.LimboIntegration;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static shadow.utils.main.AlixUtils.requireCaptchaVerification;

public final class UnverifiedUser extends AbstractAlixCtxUser {

    private static final ByteBuf loginSuccessMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("login-success"));
    private final Player player;
    private final PersistentUserData data; //<- Can be null (other variables may be null as well, but this one is decently important)
    private final User retrooperUser;
    public final ByteBufHarvester bufHarvester;
    //A captcha future that is very likely to be already finished, but we do not have the certainty that it is.
    //It is also very likely to be completed if generated at runtime for a user before access. Even if
    //accessed before completion, the captcha generation takes less than 35 ms (for a map captcha) and has already been started before
    //AlixFuture#whenCompleted was invoked, so it will take even less. More than that, all consumer acceptance
    //is done async to the currently executing thread
    private final AlixFuture<Captcha> captchaFuture;
    private final ScheduledFuture<?> reminderTask;
    //private final PacketInterceptor duplexHandler;
    private final String strAddress;
    private final InetAddress address;
    //private PacketProcessor processor;
    private final boolean captchaInitialized, originalCollidableState, joinedRegistered, isBedrock;
    private final PotionEffectHandler potionEffectHandler;
    private final VerificationMessage verificationMessage;
    private PacketBlocker blocker;
    private LoginType loginType;
    private LoginVerification loginVerification;
    private AlixVerificationGui alixGui;
    public int loginAttempts, captchaAttempts, authAppAttempts;
    private boolean hasCompletedCaptcha, isGuiUser;//, isGUIInitialized;
    //Virtualization values
    public boolean blindnessSent;
    public String originalJoinMessage;
    //public Location originalSpawnEventLocation;
    //AntiBot values
    public boolean keepAliveReceived;//, armSwingReceived;
    //public long armSwingSent, keepAliveSent;

    public UnverifiedUser(Player player, TemporaryUser tempUser) {
        super(NettyUtils.getSilentContext(tempUser.getChannel()));
        //UserManager.putAttr(this);
        this.player = player;
        this.data = tempUser.getLoginInfo().getData();
        this.strAddress = tempUser.getLoginInfo().getTextIP();
        this.address = tempUser.getLoginInfo().getIP();
        this.retrooperUser = tempUser.retrooperUser();
        this.bufHarvester = tempUser.bufHarvester();

        //potion effect saving
        this.potionEffectHandler = PotionEffectHandler.newHandlerFor(this);
        this.potionEffectHandler.resetEffects();

        //this.player.setPersistent(false); //Do not save the player

        //Main.logWarning("UNV USER:  " + this.getChannel().pipeline().names());

        this.originalCollidableState = player.isCollidable();//save the original collidable state
        this.player.setCollidable(false); // <- excluding the player from the collision calculation to save a bit of cpu & to stop entity collision at verification
        //this.player.setPersistent(false); //Explicitly state to not save the semi-virtualized user

        //common handling
        boolean hasAccount = hasAccount();//data != null
        boolean registered = isRegistered();

        //auth app support
        boolean justAuthApp = registered && this.data.getLoginParams().getAuthSettings() == AuthSetting.AUTH_APP;

        //bedrock support
        Object bedrockPlayer = Dependencies.getBedrockPlayer(this.player);
        this.isBedrock = bedrockPlayer != null;

        this.joinedRegistered = registered;
        this.hasCompletedCaptcha = this.isBedrock || hasAccount || !requireCaptchaVerification//is a bedrock player, has an account or captcha is disabled
                || LimboIntegration.hasCompletedCaptcha(tempUser.getChannel(), player.getName());//passed the check in the virtual limbo server
        this.captchaInitialized = !hasCompletedCaptcha;//the captcha (will be) initialized if the user is required to complete it

        this.loginType = hasAccount ? data.getLoginType() : ConfigParams.defaultLoginType;
        this.isGuiUser = this.loginType != LoginType.COMMAND;

        if (player.isDead()) this.onSyncDeath();

        this.captchaFuture = this.captchaInitialized ? Captcha.nextCaptcha() : null;//Fast captcha get and request for a new captcha generation or null if disabled
        this.verificationMessage = VerificationMessage.createFor(this);

        if (isBedrock) this.alixGui = PasswordGui.newBuilderBedrock(this, bedrockPlayer);
        else if (justAuthApp) this.alixGui = PasswordGui.newBuilder2FA(this);
        else if (isGuiUser && hasCompletedCaptcha)
            this.alixGui = PasswordGui.newBuilder(this, this.loginType);
        //else this.getChannel().eventLoop().schedule(() -> this.setVerificationMessageBuffer(getVerificationReminderMessagePacket(registered, hasAccount)), 500, TimeUnit.MILLISECONDS);

        if (registered && !justAuthApp) this.loginVerification = new LoginVerification(this.data.getPassword(), true);

        this.blocker = justAuthApp ? PacketBlocker.getPacketBlocker2FA(this, tempUser) : PacketBlocker.getPacketBlocker(this, tempUser, this.loginType);
        this.reminderTask = VerificationReminder.reminderFor(this);//probably the most efficient way, since all packet sending methods need to be invoked on the eventLoop thread
        //this.openPasswordBuilderGUI();
        //if (!captchaInitialized) this.spoofVerificationPackets();//spoof the verification packets immediately
    }


    public void spoofVerificationPackets() {
        //DEBUG_TIME();
        //Main.logError("VERIFY PACKETS SPOOF");
        //Main.logError("PIPELINE: " + this.getChannel().pipeline().names());
        if (!isGuiUser && !isBedrock) this.verificationMessage.updateMessage();

        CommandsPacketManager.write(this);
        AlixHandler.sendLoginEffectsPackets(this);

        if (captchaInitialized)
            this.captchaFuture.whenCompleted(c -> c.sendPackets(this), this.getChannel().eventLoop());
        else this.openVerificationGUI();
        /*this.getChannel().eventLoop().schedule(() -> {
            this.writeAndFlushConstSilently(OutPositionPacketConstructor.CAPTCHA_WORLD_TELEPORT);
            Main.logError("SENT");
        }, 3, TimeUnit.SECONDS);*/
    }

    public void onSyncDeath() {
        this.syncRespawn();
        this.getChannel().eventLoop().schedule(() -> {
            AlixHandler.sendLoginEffectsPackets(this);
            this.blocker.getFallPhase().tpPosCorrect();
            //this.blocker.getFallPhase().noCobwebSpoof();
        }, 1, TimeUnit.SECONDS);
    }

    private void syncRespawn() {//Manually respawn the player
        AlixScheduler.runLaterSync(() -> this.player.spigot().respawn(), 500, TimeUnit.MILLISECONDS);
    }

    public void uninjectOnQuit() {
        this.blocker.releaseBlocked();
        this.uninject();
    }

    public void uninject() {//reverses back all changes (but does not teleport back)
        //release allocated
        if (captchaInitialized)
            Captcha.uninject(this.captchaFuture); //this.captchaFuture.whenCompleted(Captcha::release);
        this.reminderTask.cancel(true);
        this.verificationMessage.destroy();
        if (this.alixGui != null) this.alixGui.destroy();

        //return collidable state
        this.player.setCollidable(this.originalCollidableState);

        //give back potion effects
        this.potionEffectHandler.returnEffects();
    }

    public boolean isBedrock() {
        return this.isBedrock;
    }

    //the action blocker is now automatically disabled by overriding the PacketProcessor
/*    private void disableActionBlockerAndUninject() {
        //this.duplexHandler.setProcessor(new SelfDelegatingProcessor(this.duplexHandler));//make sure no problems occur by allowing all packets to flow (?)
        this.uninject();
    }*/

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public PacketBlocker getPacketBlocker() {
        return this.blocker;
    }

    @Nullable
    public PersistentUserData getData() {
        return this.data;
    }

    @NotNull
    public String getStrAddress() {
        return this.strAddress;
    }

    @NotNull
    public InetAddress getIPAddress() {
        return this.address;
    }

    @Nullable
    public AlixFuture<Captcha> getCaptchaFuture() {
        return captchaFuture;
    }

    @NotNull
    public VerificationMessage getVerificationMessage() {
        return verificationMessage;
    }

    /*    @NotNull
    public Location getCurrentLocation() {
        return AlixWorld.TELEPORT_LOCATION;//The current location should be the captcha world (set by the OfflineExecutors)
    }*/

    //was or is
    public boolean captchaInitialized() {
        return captchaInitialized;
    }

    public boolean isCaptchaCorrect(String s) {
        return this.captchaInitialized && this.captchaFuture.hasCompleted() && this.captchaFuture.value().isCorrect(s);
    }


    @Nullable
    public AlixVerificationGui getPasswordBuilder() {
        return alixGui;
    }

/*    public boolean isGuiUser() {
        return isGuiUser;
    }*/

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

    public void completeCaptcha() {
        //if (this.hasCompletedCaptcha) return;
        this.hasCompletedCaptcha = true;

        if (!this.isBedrock()) this.verificationMessage.updateAfterCaptchaComplete();
        CommandsPacketManager.writeAndFlush(this);
        //this.writeAndFlushRaw(this.rawVerificationMessageBuffer);

        this.blocker.startLoginKickTask();
        this.captchaFuture.value().onCompletion(this);//It must've been completed if this method was invoked

        if (this.isGuiUser) this.initGUI();
        //else if (this.isBedrock) this.openVerificationGUI(); //<- bedrock players never get a captcha challenge
    }

    private void initGUI() {
        this.alixGui = PasswordGui.newBuilder(this, this.loginType);
        this.blocker.updateBuilder();
        this.openVerificationGUI();
    }

    /*private void initGUI0() {
        //this.player.closeInventory();
        this.openPasswordBuilderGUI0();
    }*/

/*    public Object getMapPacket() {
        return ((MapCaptcha) captcha).
    }*/

    public AlixVerificationGui getVerificationGUI() {
        return alixGui;
    }

    public boolean isGUIUser() {
        return isGuiUser;
    }

/*    public boolean isGUIInitialized() {
        return isGUIInitialized;
    }

    public void setGUIInitialized(boolean initialized) {
        this.isGUIInitialized = initialized;
    }*/

    public void openVerificationGUI() {
        if (this.alixGui != null) {
            ///this.isGUIInitialized = true;
            this.alixGui.openGUI();
        }
    }

    public boolean hasCompletedCaptcha() {
        return hasCompletedCaptcha;
    }

    private boolean init2FA() {
        if (this.data.getLoginParams().getAuthSettings() != AuthSetting.PASSWORD_AND_AUTH_APP) return false;
        this.verificationMessage.clearEffects();

        if (isGuiUser) {
            this.alixGui.destroy();
            MethodProvider.closeInventoryAsyncSilently(this.silentContext());//we do not care that it's silent
            ///this.isGUIInitialized = false;
        }

        this.isGuiUser = true;

        this.blocker = PacketBlocker.getPacketBlocker2FA(this.blocker);//uses the previous blocker
        this.alixGui = PasswordGui.newBuilder2FA(this);

        this.openVerificationGUI();
        return true;
    }

    private boolean initDoubleVer() {
        if (data.getLoginParams().isDoubleVerificationEnabled() && loginVerification.isPhase1()) {
            if (isGuiUser) {
                if (this.alixGui != null) {
                    this.alixGui.destroy();
                    this.alixGui = null;
                }
                MethodProvider.closeInventoryAsyncSilently(this.silentContext());//we do not care that it's silent
                //this.retrooperUser.closeInventory();
                ///this.isGUIInitialized = false;
            }

            this.loginType = data.getLoginParams().getExtraLoginType();
            this.isGuiUser = loginType != LoginType.COMMAND;

            this.blocker = PacketBlocker.getPacketBlocker(this.blocker, this.loginType);//uses the previous blocker
            //this.duplexHandler.setProcessor(this.blocker);

            if (isGuiUser) {
                this.verificationMessage.clearEffects();//skipping bedrock check, since double ver isn't available for bedrock players
                this.initGUI();
            } else this.verificationMessage.updateMessage();//spoof for when title is used
            /*else {
                this.verificationMessageBuffer = AlixUtils.notLoggedInUserMessagePacket;
                this.writeAndFlushConstSilently(this.verificationMessageBuffer);
            }*/

            this.loginVerification = new LoginVerification(data.getLoginParams().getExtraPassword(), false);
            return true;
        }
        return false;
    }

    //Verifications.remove(this.player);//remove the player immediately because of possible complications later, such as teleportation in OfflineExecutors


    private CompletableFuture<Boolean> unvirtualizeAndTeleportBack() {//must be invoked sync
        //this.player.setPersistent(true);//the user is no longer virtualized, so allow his data to be saved again
        this.player.setCollidable(this.originalCollidableState);//return the original collidable state

        //Location loc = this.originalSpawnEventLocation; //UserSemiVirtualization.invokeVirtualizedSpawnLocationEventNoTeleport(this);//unvirtualize spawn loc

        //CompletableFuture<Boolean> future = loc.getWorld().equals(AlixWorld.CAPTCHA_WORLD) ? OriginalLocationsManager.teleportBack(player) : MethodProvider.teleportAsync(player, loc);
        CompletableFuture<Boolean> future = OriginalLocationsManager.teleportBack(player);

        //future.thenAccept(b -> UserSemiVirtualization.invokeVirtualizedJoinEvent(this));//unvirtualize join event

        return future;
    }

    private static final AlixMessage
            captchaJoinMessage = Messages.getAsObject("log-player-join-captcha-verified"),
            registerJoinMessage = Messages.getAsObject("log-player-join-registered"),
            loginJoinMessage = Messages.getAsObject("log-player-join-logged-in");

    @OptimizationCandidate
    private void sendJoinMessage() {
        AlixScheduler.async(() -> {
            AlixMessage msg = this.captchaInitialized ? captchaJoinMessage : this.joinedRegistered ? loginJoinMessage : registerJoinMessage;
            Main.logInfo(msg.format(this.player.getName(), this.strAddress));

            if (this.originalJoinMessage != null) {
                //AlixUtils.broadcastRaw(this.originalJoinMessage);
                AlixUtils.serverLog(this.originalJoinMessage);
                ByteBuf constMsgBuf = OutMessagePacketConstructor.constructConst(this.originalJoinMessage);
                for (AlixUser u : UserManager.users())
                    if (u.isVerified() && u.silentContext() != this.silentContext() && u.silentContext() != null
                            && u.retrooperUser().getEncoderState() == ConnectionState.PLAY)
                        u.writeAndFlushConstSilently(constMsgBuf);

                this.writeAndFlushConstSilently(constMsgBuf);
                constMsgBuf.unwrap().release();//maybe optimize this later?
            }
        });
    }

    public void tryLogIn() {
        if (initDoubleVer()) return;
        if (init2FA()) return;
        this.logIn();
    }

    public void logIn() {
        AlixScheduler.async(() -> {
            this.logIn0();
            AlixScheduler.sync(this::logIn1);
        });
    }

    private void logIn0() {
        this.writeConstSilently(loginSuccessMessagePacket);//invoked here, since the this::initDoubleVer can prevent this method from being invoked
        //this.data.updateLastSuccessfulLoginTime();
        VerifiedUser verifiedUser = UserManager.addVerifiedUser(player, data, this.getIPAddress(), retrooperUser, this.silentContext());//invoked before onSuccessfulVerification to remove UnverifiedUser from UserManager, to indicate that he's verified
        AlixEventInvoker.callOnAuthInferThread(AuthReason.MANUAL_LOGIN, verifiedUser);
        this.onSuccessfulVerification();
        AlixHandler.resetBlindness(this);
        //AlixScheduler.runLaterAsync(() -> ReflectionUtils.resetLoginEffectPackets(this), 1, TimeUnit.SECONDS);
    }

    private void logIn1() {
        //this.player.setGameMode(this.originalGameMode);
        this.unvirtualizeAndTeleportBack();
        AlixUtils.loginCommandList.invoke(player);
        //this.player.setGameMode(this.originalGameMode);
    }

/*    public void registerSync(String password) {//invoked sync
        try {
            this.register0(password);
        } finally {
            this.register1();//invoke this method at all cost
        }
    }*/

    public void registerAsync(String password) {//invoked async
        try {
            this.register0(password);
        } finally {
            AlixScheduler.sync(this::register1);//invoke this method at all cost
        }
    }

    private static final boolean autoIpAutoLoginAsk = Main.config.getBoolean("auto-ip-autologin-ask");

    private void register1() {
        //this.player.setGameMode(this.originalGameMode);
        CompletableFuture<Boolean> future = this.unvirtualizeAndTeleportBack();

        AlixUtils.registerCommandList.invoke(player);
        //if (captchaInitialized) GeoIPTracker.removeIP(this.ipAddress);
        if (autoIpAutoLoginAsk && !hasAccount())
            future.thenAccept(b -> IpAutoLoginGUI.add(this.player));//make sure to open the gui after the teleport, as it will close otherwise

        //future.thenAccept(b -> Main.logError("TELEPOOOOOOORT"));
        //this.player.setGameMode(this.originalGameMode);
    }

    private void register0(String password) {//the common part
        //AlixScheduler.async(() -> {
        VerifiedUser verifiedUser = UserManager.register(this.player, password, this.getIPAddress(), this.retrooperUser, this.silentContext());
        PersistentUserData data = verifiedUser.getData();

        AlixEventInvoker.callOnAuthInferThread(AuthReason.MANUAL_REGISTER, verifiedUser);
        //data.updateLastSuccessfulLoginTime();
        data.setLoginType(this.loginType);

        if (data.getPremiumData().getStatus().isUnknown())
            data.setPremiumData(PremiumData.NON_PREMIUM);//if the player wasn't automatically logged in, we can assume he's non-premium

        this.onSuccessfulVerification();
        AlixHandler.resetBlindness(this);
        /*if (captchaInitialized && originalJoinMessage != null)
            AlixUtils.broadcastRaw(this.originalJoinMessage);//only send the join message if it wasn't removed by someone else*/
        //});
    }

    private void onSuccessfulVerification() {
        this.uninject();
        this.sendJoinMessage();
        if (!isBedrock) this.verificationMessage.clearEffects();
        //Main.logError("PRE TO SEND");
        this.blocker.sendBlockedPackets();
        //Main.logError("POST TO SEND");
        this.blocker = PacketBlocker.EMPTY;//important for the user to receive refresh packets from the server (as a side effect of a world change)
        //this.player.setPersistent(true); //Start saving the verified player
    }

    public boolean isPasswordCorrect(String s) {
        return this.loginVerification.isPasswordCorrect(s); //data.getPassword().isEqualTo(s);
    }

    public boolean hasAccount() {
        return data != null;
    }

    public boolean isRegistered() {
        return PersistentUserData.isRegistered(this.data); //this.hasAccount() && data.getPassword().isSet();
    }

    @Override
    public User retrooperUser() {
        return this.retrooperUser;
    }

    @Override
    public PacketProcessor getPacketProcessor() {
        return this.blocker;
    }

    @Override
    public boolean isVerified() {
        return false;
    }

    @Override
    public ByteBufHarvester bufHarvester() {
        return this.bufHarvester;
    }
}