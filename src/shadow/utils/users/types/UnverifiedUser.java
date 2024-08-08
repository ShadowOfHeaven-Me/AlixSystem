package shadow.utils.users.types;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.runnables.futures.AlixFuture;
import alix.common.utils.config.ConfigParams;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.Main;
import shadow.systems.gui.impl.IpAutoLoginGUI;
import shadow.systems.login.LoginVerification;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.misc.effect.PotionEffectHandler;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.ByteBufHarvester;
import shadow.utils.netty.unsafe.UnsafeNettyUtils;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.UserManager;
import shadow.utils.world.AlixWorld;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import static shadow.utils.main.AlixUtils.getVerificationReminderMessagePacket;
import static shadow.utils.main.AlixUtils.requireCaptchaVerification;

public final class UnverifiedUser implements AlixUser {

    //private static final String registerSuccess = Messages.get("register-success");
    private static final ByteBuf loginSuccessMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("login-success"));
    private final Player player;
    private final PersistentUserData data; //<- Can be null (other variables may be null as well, but this one is decently important)
    private final User retrooperUser;
    public final ByteBufHarvester bufHarvester;
    private final ChannelHandlerContext silentContext;
    //A captcha future that is very likely to be already finished, but we do not have the certainty that it is.
    //It is also very likely to be completed if generated at runtime for a user before it is accessed, and even if
    //accessed before completion, the captcha generation takes less than 35 ms (for a map captcha) and has already been started before
    //AlixFuture#whenCompleted was invoked, so it will take even less. More than that, all consumer acceptance
    //is done async to the currently executing thread (if specified, currently on the eventLoop).
    private final AlixFuture<Captcha> captchaFuture;
    private final ScheduledFuture<?> reminderTask;
    //private final PacketInterceptor duplexHandler;
    private final String strAddress;
    private final InetAddress address;
    //private PacketProcessor processor;
    //private final Location currentLocation;
    //private final GameMode originalGameMode;
    private final boolean captchaInitialized, originalCollidableState;
    private final boolean joinedRegistered;
    private final PotionEffectHandler potionEffectHandler;
    private PacketBlocker blocker;
    private LoginType loginType;
    private LoginVerification loginVerification;
    private AlixVerificationGui alixGui;
    private volatile ByteBuf rawVerificationMessageBuffer;
    public long nextSend;
    public int loginAttempts, captchaAttempts;
    private boolean hasCompletedCaptcha, isGUIInitialized, isGuiUser;
    //Virtualization values
    public boolean blindnessSent;
    public String originalJoinMessage;
    public Location originalSpawnEventLocation;

    public UnverifiedUser(Player player, TemporaryUser tempUser) {
        this.player = player;
        this.data = tempUser.getLoginInfo().getData();
        this.strAddress = tempUser.getLoginInfo().getTextIP();
        this.address = tempUser.getLoginInfo().getIP();
        this.retrooperUser = tempUser.reetrooperUser();
        this.bufHarvester = tempUser.getBufHarvester();
        this.silentContext = NettyUtils.getSilentContext(tempUser.getChannel());

        //potion effect saving
        this.potionEffectHandler = PotionEffectHandler.newHandlerFor(this);
        this.potionEffectHandler.resetEffects();

        //this.player.setPersistent(false); //Do not save the player

        //Main.logWarning("UNV USER:  " + this.getChannel().pipeline().names());

        this.originalCollidableState = player.isCollidable();//save the original collidable state
        this.player.setCollidable(false); // <- excluding the player from the collision calculation to save a bit of cpu & to stop entity collision at verification
        //this.player.setPersistent(false); //Explicitly state to not save the semi-virtualized user

        boolean hasAccount = hasAccount();//data != null
        boolean registered = isRegistered();

        this.joinedRegistered = registered;
        this.hasCompletedCaptcha = hasAccount || !requireCaptchaVerification;//has an account or captcha is disabled
        this.captchaInitialized = !hasCompletedCaptcha;//the captcha (will be) initialized if the user is required to complete it

        this.loginType = hasAccount ? data.getLoginType() : ConfigParams.defaultLoginType;
        this.isGuiUser = this.loginType != LoginType.COMMAND;

        if (player.isDead()) player.spigot().respawn();

        this.captchaFuture = this.captchaInitialized ? Captcha.nextCaptcha() : null;//Fast captcha get and request for a new captcha generation or null if disabled

        if (isGuiUser && hasCompletedCaptcha) this.alixGui = PasswordGui.newBuilder(this, this.loginType);
        //else this.getChannel().eventLoop().schedule(() -> this.setVerificationMessageBuffer(getVerificationReminderMessagePacket(registered, hasAccount)), 500, TimeUnit.MILLISECONDS);

        if (registered) this.loginVerification = new LoginVerification(this.data.getPassword(), true);

        this.blocker = PacketBlocker.getPacketBlocker(this, this.loginType);
        this.reminderTask = VerificationReminder.reminderFor(this);//probably the most efficient way, since all packet sending methods need to be invoked on the eventLoop thread
        //this.openPasswordBuilderGUI();
        //if (!captchaInitialized) this.spoofVerificationPackets();//spoof the verification packets immediately
    }

    public void spoofVerificationPackets() {
        //DEBUG_TIME();
        if (!isGUIInitialized) this.setVerificationMessageBuffer(getVerificationReminderMessagePacket(this.isRegistered(), this.hasAccount()));

        AlixHandler.sendLoginEffectsPackets(this);
        if (captchaInitialized)
            this.captchaFuture.whenCompleted(c -> c.sendPackets(this), this.getChannel().eventLoop());
        this.openPasswordBuilderGUI();
        /*this.getChannel().eventLoop().schedule(() -> {
            this.writeAndFlushConstSilently(OutPositionPacketConstructor.CAPTCHA_WORLD_TELEPORT);
            Main.logError("SENT");
        }, 3, TimeUnit.SECONDS);*/
    }

    public void uninjectOnQuit() {
        this.blocker.releaseBlocked();
        this.uninject();
    }

    public void uninject() {//reverses back all changes (but does not teleport back)
        //if (captchaInitialized) captcha.uninject(this);
        if (captchaInitialized) this.captchaFuture.whenCompleted(Captcha::release);
        this.reminderTask.cancel(true);
        this.releaseVerificationMessageBuffer();

        //give back the effects
        this.potionEffectHandler.returnEffects();

        if (isGUIInitialized) this.alixGui.getVirtualGUI().destroy();
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

    /*    @NotNull
    public Location getCurrentLocation() {
        return AlixWorld.TELEPORT_LOCATION;//The current location should be the captcha world (set by the OfflineExecutors)
    }*/

    public boolean isCaptchaInitialized() {
        return captchaInitialized;
    }

    public boolean isCaptchaCorrect(String s) {
        return this.captchaInitialized && this.captchaFuture.hasCompleted() && this.captchaFuture.value().isCorrect(s);
    }

    @Nullable
    public ByteBuf getRawVerificationMessageBuffer() {
        return rawVerificationMessageBuffer;
    }

    private void setVerificationMessageBuffer(ByteBuf verificationMessageBuffer) {
        this.releaseVerificationMessageBuffer();

        //long t0 = System.nanoTime();
        this.rawVerificationMessageBuffer = UnsafeNettyUtils.sendAndGetRaw(this.silentContext, this.bufHarvester, NettyUtils::constBuffer, verificationMessageBuffer.duplicate());
        //long diff0 = System.nanoTime() - t0;
        //Main.logError("ALLOCATING MESSAGE TIME: " + diff0 / Math.pow(10, 6) + "ms");
    }

    private void releaseVerificationMessageBuffer() {
        if (this.rawVerificationMessageBuffer != null) {
            //Main.logError("BUFFER: " + this.rawVerificationMessageBuffer + " " + this.rawVerificationMessageBuffer.unwrap());
            this.rawVerificationMessageBuffer.unwrap().release();//it's unreleasable - unwrap
            this.rawVerificationMessageBuffer = null;
        }
    }

    @Nullable
    public AlixVerificationGui getPasswordBuilder() {
        return alixGui;
    }

    public boolean isGuiUser() {
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

    public void completeCaptcha() {
        //if (this.hasCompletedCaptcha) return;
        this.setVerificationMessageBuffer(AlixUtils.unregisteredUserMessagePacket);
        this.writeAndFlushRaw(this.rawVerificationMessageBuffer);

        this.blocker.startLoginKickTask();
        this.captchaFuture.value().onCompletion(this);//It must've been completed if this method was invoked

        this.hasCompletedCaptcha = true;

        if (this.isGuiUser) this.initGUI();
    }

    private void initGUI() {
        this.alixGui = PasswordGui.newBuilder(this, this.loginType);
        this.blocker.updateBuilder();
        this.openPasswordBuilderGUI();
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

    public boolean isGUIInitialized() {
        return isGUIInitialized;
    }

    public void setGUIInitialized(boolean initialized) {
        this.isGUIInitialized = initialized;
    }

    public void openPasswordBuilderGUI() {
        if (isGUIInitialized) this.alixGui.openGUI();
    }

    public boolean hasCompletedCaptcha() {
        return hasCompletedCaptcha;
    }

    private boolean initDoubleVer() {
        if (data.getLoginParams().isDoubleVerificationEnabled() && loginVerification.isPhase1()) {
            if (isGuiUser) {
                if (isGUIInitialized) this.alixGui.getVirtualGUI().destroy();
                MethodProvider.closeInventoryAsyncSilently(this.silentContext);//we do not care that it's silent
                //this.retrooperUser.closeInventory();
                this.isGUIInitialized = false;
            }
            this.loginType = data.getLoginParams().getExtraLoginType();
            this.isGuiUser = loginType != LoginType.COMMAND;

            this.blocker = PacketBlocker.getPacketBlocker(this.blocker, this.loginType);//uses the previous blocker
            //this.duplexHandler.setProcessor(this.blocker);

            if (isGuiUser) this.initGUI();
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

        Location loc = this.originalSpawnEventLocation; //UserSemiVirtualization.invokeVirtualizedSpawnLocationEventNoTeleport(this);//unvirtualize spawn loc

        CompletableFuture<Boolean> future = loc.getWorld().equals(AlixWorld.CAPTCHA_WORLD) ? OriginalLocationsManager.teleportBack(player) : MethodProvider.teleportAsync(player, loc);
        //future.thenAccept(b -> UserSemiVirtualization.invokeVirtualizedJoinEvent(this));//unvirtualize join event

        return future;
    }

    private static final AlixMessage
            captchaJoinMessage = Messages.getAsObject("log-player-join-captcha-verified"),
            registerJoinMessage = Messages.getAsObject("log-player-join-registered"),
            loginJoinMessage = Messages.getAsObject("log-player-join-logged-in");

    private void sendJoinMessage() {
        AlixScheduler.async(() -> {
            AlixMessage msg = this.captchaInitialized ? captchaJoinMessage : this.joinedRegistered ? loginJoinMessage : registerJoinMessage;
            Main.logInfo(msg.format(this.player.getName(), this.strAddress));

            if (this.originalJoinMessage != null) {
                //AlixUtils.broadcastRaw(this.originalJoinMessage);
                AlixUtils.serverLog(this.originalJoinMessage);
                ByteBuf constMsgBuf = OutMessagePacketConstructor.constructConst(this.originalJoinMessage);
                for (AlixUser u : UserManager.users())
                    if (u.isVerified() && u.silentContext() != this.silentContext && u.silentContext() != null)
                        u.writeAndFlushConstSilently(constMsgBuf);

                this.writeAndFlushConstSilently(constMsgBuf);
                constMsgBuf.unwrap().release();//maybe optimize this later?
            }
        });
    }

    public void logIn() {
        if (initDoubleVer()) return;
        AlixScheduler.async(() -> {
            this.logIn0();
            AlixScheduler.sync(this::logIn1);
        });
    }

    private void logIn0() {
        this.writeAndFlushConstSilently(loginSuccessMessagePacket);//invoked here, since the this#initDoubleVer can prevent this method from being invoked
        UserManager.addVerifiedUser(player, data, this.getIPAddress(), retrooperUser, silentContext);//invoked before onSuccessfulVerification to remove UnverifiedUser from UserManager, to indicate that he's verified
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
        PersistentUserData data = UserManager.register(this.player, password, this.getIPAddress(), this.retrooperUser, this.silentContext);
        data.setLoginType(this.loginType);
        this.onSuccessfulVerification();
        AlixHandler.resetBlindness(this);

        /*if (captchaInitialized && originalJoinMessage != null)
            AlixUtils.broadcastRaw(this.originalJoinMessage);//only send the join message if it wasn't removed by someone else*/
        //});
    }

    private void onSuccessfulVerification() {
        this.uninject();
        this.sendJoinMessage();
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
        return hasAccount() && data.getPassword().isSet();
    }

    @Override
    public User reetrooperUser() {
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
    public Channel getChannel() {
        return this.silentContext.channel();
    }

    @Override
    public ChannelHandlerContext silentContext() {
        return this.silentContext;
    }
}