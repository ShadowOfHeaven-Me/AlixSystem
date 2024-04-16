package shadow.utils.users.types;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.runnables.futures.AlixFuture;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.gui.impl.IpAutoLoginGUI;
import shadow.systems.login.LoginVerification;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.netty.NettyUtils;
import shadow.utils.objects.packet.PacketProcessor;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.UserManager;
import shadow.utils.world.AlixWorld;

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
    private final ChannelHandlerContext silentContext;
    //A captcha future that is very likely to be already finished, but we do not have the certainty that it is.
    //It is also very likely to be completed if generated at runtime for a user before it is accessed, and even if
    //accessed before completion, the captcha generation takes less than 35 ms (for a map captcha) and has already been started before
    //AlixFuture#whenCompleted was invoked, so it will take even less. More than that, all consumer acceptance
    //is done async to the currently executing thread (if specified, currently on the eventLoop).
    private final AlixFuture<Captcha> captchaFuture;
    private final ScheduledFuture<?> reminderTask;
    //private final PacketInterceptor duplexHandler;
    private PacketBlocker blocker;
    //private final Location currentLocation;
    //private final GameMode originalGameMode;
    private final String ipAddress, joinMessage;
    private final boolean captchaInitialized, originalCollidableState;
    private LoginType loginType;
    private LoginVerification loginVerification;
    private AlixVerificationGui alixGui;
    private ByteBuf verificationMessageBuffer;
    public long nextSend = System.currentTimeMillis();
    public int loginAttempts, captchaAttempts;
    private boolean hasCompletedCaptcha, isGUIInitialized, isGuiUser;

    public UnverifiedUser(Player player, TemporaryUser tempUser, String joinMessage) {
        this.player = player;
        this.data = tempUser.getLoginInfo().getData();
        this.ipAddress = tempUser.getLoginInfo().getIP();
        this.retrooperUser = tempUser.reetrooperUser();
        this.joinMessage = joinMessage;
        this.silentContext = NettyUtils.getSilentContext(tempUser.getChannel());

        //Main.logWarning("UNV USER:  " + this.getChannel().pipeline().names());

        this.originalCollidableState = player.isCollidable();//primitive way of saving the original collidable state
        this.player.setCollidable(false); // <- excluding the player from the collision calculation to save a bit of cpu & to stop entity collision at verification

        boolean hasAccount = hasAccount();//data != null
        boolean registered = isRegistered();

        this.hasCompletedCaptcha = hasAccount || !requireCaptchaVerification;//has an account or captcha is disabled
        this.captchaInitialized = !hasCompletedCaptcha;//the captcha was initialized if the user is required to complete it

        this.loginType = hasAccount ? data.getLoginType() : AlixUtils.defaultLoginType;
        this.isGuiUser = this.loginType != LoginType.COMMAND;

        //new PacketInterceptor(this.player);//the delegator to the processor
        //this.isGuiUser = data != null ? data.getPasswordType() == PasswordType.PIN || AlixUtils.anvilPasswordGui : AlixUtils.anvilPasswordGui || AlixUtils.defaultPasswordType == PasswordType.PIN;

        //this.originalGameMode = player.getGameMode();
        //this.player.setGameMode(GameMode.ADVENTURE); //Set the gamemode to adventure (on spectator the map is invisible and on creative the countdown is - also it prevents a possible accidental arm animation packet spam)

        if (player.isDead()) player.spigot().respawn();

        if (this.captchaInitialized) {
            this.captchaFuture = Captcha.nextCaptcha(); //Fast captcha get and request for a new captcha generation
            this.player.setPersistent(false); //Do not save the possibly bot player
        } else this.captchaFuture = null;

        if (isGuiUser && hasCompletedCaptcha)
            this.alixGui = PasswordGui.newBuilder(this, this.loginType);
        else this.verificationMessageBuffer = getVerificationReminderMessagePacket(registered, hasAccount);

        if (registered) this.loginVerification = new LoginVerification(this.data.getPassword(), true);

        this.blocker = PacketBlocker.getPacketBlocker(this, this.loginType);//the actual processor
        //this.duplexHandler.setProcessor(this.blocker);//setting the processor
        this.reminderTask = VerificationReminder.reminderFor(this);//probably the most efficient way, since all packet sending methods need to be invoked on the eventLoop thread
        //this.openPasswordBuilderGUI();
        //if (!captchaInitialized) this.spoofVerificationPackets();//spoof the verification packets immediately
    }

    //private static final ProtocolManager protocolManager = PacketEvents.getAPI().getProtocolManager();

    public void writeDynamicMessageSilently(Component message) {
        NettyUtils.writeDynamicWrapper(OutMessagePacketConstructor.packetWrapper(message), this.silentContext);
    }

    public void sendDynamicMessageSilently(String message) {
        NettyUtils.writeAndFlushDynamicWrapper(OutMessagePacketConstructor.packetWrapper(Component.text(message)), this.silentContext);
    }

    //According to PacketTransformationUtil.transform(PacketWrapper<?>), this way of sending the packets silently is just fine
    public void writeUnreadSilently(PacketWrapper<?> packet) {
        NettyUtils.writeDynamicWrapper(packet, this.silentContext);
        //this.silentContext.write(NettyUtils.createBuffer(packet, initialBufferCapacity));
    }

    public void writeAndFlushUnreadSilently(PacketWrapper<?> packet) {
        NettyUtils.writeAndFlushDynamicWrapper(packet, this.silentContext);
        //this.silentContext.writeAndFlush(NettyUtils.createBuffer(packet, initialBufferCapacity));
    }

    public void writeSilently(ByteBuf buffer) {
        this.silentContext.write(buffer);
    }

    public void writeAndFlushSilently(ByteBuf buffer) {
        this.silentContext.writeAndFlush(buffer);
    }

    public void flushSilently() {
        this.silentContext.flush();
    }

    public void writeConstSilently(ByteBuf buf) {
        NettyUtils.writeConst(this.silentContext, buf);
        //protocolManager.writePacketSilently(this.channel,
    }

    public void writeAndFlushConstSilently(ByteBuf buf) {
        //retrooperUser.sendPacketSilently(
        NettyUtils.writeAndFlushConst(this.silentContext, buf);
        //protocolManager.writePacketSilently(this.channel,
    }

    public void spoofVerificationPackets() {
        ReflectionUtils.sendLoginEffectsPackets(this);
        if (captchaInitialized)
            this.captchaFuture.whenCompleted(c -> c.sendPackets(this), this.getChannel().eventLoop());
        else if (isGUIInitialized) AlixScheduler.sync(this::openPasswordBuilderGUI);
    }

    public void uninject() {//removes all that was ever assigned and related (but does not teleport back)
        //if (captchaInitialized) captcha.uninject(this);
        if (captchaInitialized) this.captchaFuture.whenCompleted(Captcha::uninject);

        this.reminderTask.cancel(true);

        if (hasCompletedCaptcha) //do not return the original params if not completed, as captcha unverified users are non-persistent
            this.player.setCollidable(this.originalCollidableState); // <- returning the player to his original collidable state
    }

    //the action blocker is now automatically disabled by overriding the PacketProcessor
    private void disableActionBlockerAndUninject() {
        //this.duplexHandler.setProcessor(new SelfDelegatingProcessor(this.duplexHandler));//make sure no problems occur by allowing all packets to flow (?)
        this.uninject();
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public PacketBlocker getPacketBlocker() {
        return blocker;
    }

    @Nullable
    public PersistentUserData getData() {
        return data;
    }

    @NotNull
    public String getIPAddress() {
        return ipAddress;
    }

    @NotNull
    public Location getCurrentLocation() {
        return AlixWorld.TELEPORT_LOCATION;//The current location should be the captcha world (set by the OfflineExecutors)
    }

    public boolean isCaptchaInitialized() {
        return captchaInitialized;
    }

    public boolean isCaptchaCorrect(String s) {
        return this.captchaInitialized && this.captchaFuture.hasCompleted() && this.captchaFuture.value().isCorrect(s);
    }

    @Nullable
    public ByteBuf getVerificationMessageBuffer() {
        return verificationMessageBuffer;
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
        this.verificationMessageBuffer = AlixUtils.unregisteredUserMessagePacket;
        this.writeAndFlushConstSilently(this.verificationMessageBuffer);

        this.blocker.startLoginKickTask();
        this.captchaFuture.value().onCompletion(this);//It must've been completed if this method was invoked

        this.hasCompletedCaptcha = true;

        if (this.isGuiUser) this.initGUIAsync();
    }

    private void initGUIAsync() {
        this.initGUI1();
        if (isGUIInitialized) AlixScheduler.sync(this::openPasswordBuilderGUI);
    }

    private void initGUISync() {
        this.initGUI1();
        this.openPasswordBuilderGUI();
    }

    private void initGUI1() {
        this.alixGui = PasswordGui.newBuilder(this, this.loginType);
        this.blocker.updateBuilder();
    }

    /*private void initGUI0() {
        //this.player.closeInventory();
        this.openPasswordBuilderGUI0();
    }*/

/*    public Object getMapPacket() {
        return ((MapCaptcha) captcha).
    }*/

    public boolean isGUIInitialized() {
        return isGUIInitialized;
    }

    public void setGUIInitialized(boolean initialized) {
        this.isGUIInitialized = initialized;
    }

    public void openPasswordBuilderGUI() {
        if (isGUIInitialized) this.player.openInventory(this.alixGui.getGUI());
    }

    public boolean hasCompletedCaptcha() {
        return hasCompletedCaptcha;
    }

    private boolean initDoubleVer() {
        if (data.getLoginParams().isDoubleVerificationEnabled() && loginVerification.isPhase1()) {
            if (isGuiUser) {
                MethodProvider.closeInventoryAsyncSilently(this.silentContext);//we do not care that it's silent
                //this.retrooperUser.closeInventory();
                this.isGUIInitialized = false;
            }
            this.loginType = data.getLoginParams().getExtraLoginType();
            this.isGuiUser = loginType != LoginType.COMMAND;

            this.blocker = PacketBlocker.getPacketBlocker(this.blocker, this.loginType);//uses the previous blocker
            //this.duplexHandler.setProcessor(this.blocker);

            if (isGuiUser) this.initGUIAsync();
            else {
                this.verificationMessageBuffer = AlixUtils.notLoggedInUserMessagePacket;
                this.writeAndFlushConstSilently(this.verificationMessageBuffer);
            }

            this.loginVerification = new LoginVerification(data.getLoginParams().getExtraPassword(), false);
            return true;
        }
        return false;
    }

    //Verifications.remove(this.player);//remove the player immediately because of possible complications later, such as teleportation in OfflineExecutors

    public void logIn() {
        if (initDoubleVer()) return;
        AlixScheduler.async(() -> {
            this.logIn0();
            AlixScheduler.sync(this::logIn1);
        });
    }

    private void logIn1() {
        //this.player.setGameMode(this.originalGameMode);
        OriginalLocationsManager.teleportBack(player);//tp back
        AlixUtils.loginCommandList.invoke(player);
        //this.player.setGameMode(this.originalGameMode);
    }

    private void logIn0() {//the common part
        this.writeAndFlushConstSilently(loginSuccessMessagePacket);//invoked here, since the this#initDoubleVer can prevent this method from being invoked
        this.onSuccessfulVerification();
        UserManager.addVerifiedUser(player, data, ipAddress, retrooperUser, silentContext);
        ReflectionUtils.resetLoginEffectPackets(this);//no need to execute this async here, since it's this method code is executed async by the AlixScheduler
        //AlixScheduler.runLaterAsync(() -> ReflectionUtils.resetLoginEffectPackets(this), 1, TimeUnit.SECONDS);
    }

    public void registerSync(String password) {//invoked sync
        try {
            this.register0(password);
        } finally {
            this.register1();//invoke this method at all cost
        }
    }

    public void registerAsync(String password) {//invoked async
        try {
            this.register0(password);
        } finally {
            AlixScheduler.sync(this::register1);//invoke this method at all cost
        }
    }

    private void register1() {
        //this.player.setGameMode(this.originalGameMode);
        CompletableFuture<Boolean> future = OriginalLocationsManager.teleportBack(player);
        AlixUtils.registerCommandList.invoke(player);
        //if (captchaInitialized) GeoIPTracker.removeIP(this.ipAddress);
        if (!hasAccount())
            future.thenAccept(b -> IpAutoLoginGUI.add(this.player));//make sure to open the gui after the teleport, as it will close otherwise
        //this.player.setGameMode(this.originalGameMode);
    }

    private void register0(String password) {//the common part
        //Since this code is executed on the Netty threads (or on the main thread on pre-1.17),
        //and the password hashing algorithm could take a millisecond or two,
        //we will execute the code async (already is, earlier in the code)
        //AlixScheduler.async(() -> {
        PersistentUserData data = UserManager.register(this.player, password, this.ipAddress, this.retrooperUser, this.silentContext);
        data.setLoginType(this.loginType);
        this.onSuccessfulVerification();
        AlixScheduler.async(() -> ReflectionUtils.resetLoginEffectPackets(this));

        if (captchaInitialized) {
            this.player.setPersistent(true); //Start saving the player which was verified by captcha
            if (joinMessage != null)
                AlixUtils.broadcastRaw(this.joinMessage);//only send the join message if it wasn't removed by someone else
        }
        //});
    }

    private void onSuccessfulVerification() {
        this.disableActionBlockerAndUninject();
        this.blocker.sendBlockedPackets();
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