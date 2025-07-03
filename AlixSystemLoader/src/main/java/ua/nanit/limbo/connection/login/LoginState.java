package ua.nanit.limbo.connection.login;

import alix.common.commands.file.CommandsFileManager;
import alix.common.data.AuthSetting;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.security.password.Password;
import alix.common.login.LoginVerdict;
import alix.common.login.LoginVerification;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.floodgate.GeyserUtil;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.keys.secret.MapSecretKey;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import ua.nanit.limbo.commands.LimboCommand;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.connection.captcha.KeepAlives;
import ua.nanit.limbo.connection.captcha.blocks.BlockPackets;
import ua.nanit.limbo.connection.login.countdown.LimboCountdown;
import ua.nanit.limbo.connection.login.gui.*;
import ua.nanit.limbo.connection.login.gui.bedrock.LimboBedrockGUI;
import ua.nanit.limbo.connection.login.packets.SoundPackets;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.PacketSnapshots;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;
import ua.nanit.limbo.protocol.packets.play.config.PacketPlayInReconfigureAck;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInClickSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInInventoryClose;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryClose;
import ua.nanit.limbo.protocol.packets.play.move.FlyingPacket;
import ua.nanit.limbo.protocol.packets.play.rename.PacketPlayInItemRename;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.function.Consumer;

import static alix.common.utils.config.ConfigProvider.config;
import static ua.nanit.limbo.connection.login.gui.LimboPinBuilder.maxLoginAttempts;
import static ua.nanit.limbo.protocol.PacketSnapshots.LOGIN_TITLE;
import static ua.nanit.limbo.protocol.PacketSnapshots.REGISTER_TITLE;

public final class LoginState implements VerifyState {

    public static final boolean requirePasswordRepeatInRegister = config.getBoolean("require-password-repeat-in-register");
    private static final PacketSnapshot
            REGISTER = requirePasswordRepeatInRegister
            ? createLimboCommand("register", Messages.get("commands-register-password-arg"), Messages.get("commands-register-password-second-arg"))
            : createLimboCommand("register", Messages.get("commands-register-password-arg")),
            LOGIN = createLimboCommand("login", Messages.get("commands-login-password-arg"));

    private static PacketSnapshot createLimboCommand(String command, String arg1Name, String arg2Name) {
        return LimboCommand.construct(CommandsFileManager.getCommand(command).getLabels(), arg1Name, arg2Name).getPacketSnapshot();
    }

    private static PacketSnapshot createLimboCommand(String command, String arg1Name) {
        return LimboCommand.construct(CommandsFileManager.getCommand(command).getLabels(), arg1Name).getPacketSnapshot();
    }

    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private LimboCountdown countdown;
    public LimboGUI gui;
    private Consumer<ClientConnection> authAction;
    private LoginVerification loginVerification;
    public PersistentUserData data;
    public boolean isRegistered;
    public int loginAttempts;

    public LoginState(ClientConnection connection) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
    }

    public PersistentUserData registerIfValid(String password, LoginType type) {
        //can be optimized by creating PacketSnapshots for constant messages
        @OptimizationCandidate
        String reason = AlixCommonUtils.getPasswordInvalidityReason(password, type);
        if (reason != null) {
            this.duplexHandler.write(SoundPackets.VILLAGER_NO);
            this.duplexHandler.writeAndFlush(PacketPlayOutMessage.withMessage(reason));
            return null;
        }

        return this.register0(password);
    }

    private PersistentUserData register0(String password) {
        if (this.data != null) this.data.setPassword(password);
        else this.data = PersistentUserData.createDefault(this.connection.getUsername(),
                this.connection.getAddress().getAddress(), Password.fromUnhashed(password));

        this.logIn();
        return this.data;
    }

    public void tryLogIn() {
        if (initDoubleVer()) return;
        if (init2FA()) return;
        this.logIn();
    }

    private boolean isAwaitingReconfigureAck;

    public void logIn() {
        if (this.authAction == null) throw new AlixException("authAction is null! Report this immediately!");

        if (this.connection.hasConfigPhase()) {
            this.duplexHandler.writeAndFlush(PacketSnapshots.RECONFIGURE);
            this.duplexHandler.disablePacketWriting = true;
            this.isAwaitingReconfigureAck = true;
            return;
        }

        AlixScheduler.async(this::logIn0);
    }

    private void logIn0() {
        this.data.setIP(this.connection.getAddress().getAddress());
        this.connection.uninjectConnection();
        this.authAction.accept(this.connection);
    }

    public boolean isPasswordCorrect(String password) {
        return this.loginVerification.isPasswordCorrect(password);
    }

    //true if the connection will stay alive (the user wasn't kicked)
    public boolean onIncorrectPassword() {
        if (++loginAttempts == maxLoginAttempts) {
            this.connection.sendPacketAndClose(incorrectPasswordKickPacket);
            return false;
        }

        this.write(SoundPackets.VILLAGER_NO);
        this.writeAndFlush(incorrectPasswordMessagePacket);
        return true;
    }


    //public static final AttributeKey<Boolean> JOINED_UNREGISTERED = AttributeKey.newInstance("alix:joined_unregistered");

    private static LoginVerdict getVerdict(PersistentUserData data) {
        if (data == null) return LoginVerdict.DISALLOWED_NO_DATA;

        if (!data.getPassword().isSet()) return LoginVerdict.DISALLOWED_PASSWORD_RESET;

        return LoginVerdict.DISALLOWED_LOGIN_REQUIRED;
    }

    @Override
    public void setData(PersistentUserData data, Consumer<ClientConnection> authAction, GeyserUtil geyserUtil) {
        this.data = data;
        this.isRegistered = PersistentUserData.isRegistered(data);
        this.authAction = authAction;

        //if (!isRegistered) this.connection.getChannel().attr(JOINED_UNREGISTERED).set(Boolean.TRUE);
        LoginInfo.set(this.connection.getChannel(), this.isRegistered, getVerdict(this.data));

        //common handling
        boolean hasAccount = data != null;
        var loginType = hasAccount ? data.getLoginType() : ConfigParams.defaultLoginType;
        var isGuiUser = loginType != LoginType.COMMAND;

        //auth app support
        boolean justAuthApp = this.isRegistered && this.data.getLoginParams().getAuthSettings() == AuthSetting.AUTH_APP;

        //bedrock support
        Object bedrockPlayer = geyserUtil.getBedrockPlayer(this.connection.getChannel());
        boolean isBedrock = bedrockPlayer != null;

        if (isBedrock) this.gui = this.newBuilderBedrock(bedrockPlayer);
        else if (justAuthApp) this.gui = this.newBuilder2FA();
        else if (isGuiUser) this.gui = this.newBuilder(loginType);

        if (this.isRegistered && !justAuthApp)
            this.loginVerification = new LoginVerification(this.data.getPassword(), true);

        this.countdown = ConfigParams.hasMaxLoginTime ? new LimboCountdown(this.connection, this.isRegistered) : null;
        //this.gui = new LimboPinBuilder(this.connection, data, this);

        /*this.gui = new LimboAuthBuilder(this.connection, MapSecretKey.fromName(this.connection.getUsername()), correct -> {
            if (correct) {
                this.logIn();
                return;
            }
        }, true);*/
    }

    @Override
    public void onLimboDisconnect() {
        if (this.countdown != null) this.countdown.cancel();
    }

    private LimboGUI newBuilderBedrock(Object bedrockPlayer) {
        return new LimboBedrockGUI(this.connection, bedrockPlayer, this);
    }

    private LimboGUI newBuilder2FA() {
        return new LimboAuthBuilder(this.connection, MapSecretKey.fromName(this.connection.getUsername()), correct -> {
            if (correct) {
                this.logIn();
                return;
            }
            this.onIncorrectPassword();
        }, true);
    }

    private LimboGUI newBuilder(LoginType type) {
        switch (type) {
            case PIN:
                return new LimboPinBuilder(this.connection, data, this);
            case ANVIL:
                return new LimboAnvilBuilder(this.connection, data, this,
                        this.isRegistered ? AnvilBuilderGoal.LOGIN : AnvilBuilderGoal.REGISTER);
            default:
                throw new AlixError("Invalid login type: " + type + "!");
        }
    }

    public static final PacketSnapshot CLOSE_INV = PacketSnapshot.of(new PacketPlayOutInventoryClose(0));

    private static final class LazyLoad {
        private static final PacketSnapshot enterSecondaryPassword = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("login-enter-secondary-password"));
    }

    private boolean init2FA() {
        if (this.data.getLoginParams().getAuthSettings() != AuthSetting.PASSWORD_AND_AUTH_APP) return false;
        //this.verificationMessage.clearEffects();

        /*if (this.gui != null) {
            this.alixGui.destroy();
            MethodProvider.closeInventoryAsyncSilently(this.silentContext());//we do not care that it's silent
            ///this.isGUIInitialized = false;
        }*/

        this.gui = this.newBuilder2FA();
        this.gui.show();
        return true;
    }

    private void writeCommands() {
        if (this.version().moreOrEqual(Version.V1_13))
            this.write(this.isRegistered ? LOGIN : REGISTER);
    }

    private boolean initDoubleVer() {
        if (data.getLoginParams().isDoubleVerificationEnabled() && loginVerification.isPhase1()) {

            var extraLoginType = data.getLoginParams().getExtraLoginType();
            var isSecondaryGui = extraLoginType != LoginType.COMMAND;

            if (this.gui != null && !isSecondaryGui) {
                this.write(CLOSE_INV);
                this.writeCommands();
                this.connection.writeTitle(this.isRegistered ? LOGIN_TITLE : REGISTER_TITLE);
            }

            if (data.getLoginType() == extraLoginType)
                this.write(LazyLoad.enterSecondaryPassword);

            if (isSecondaryGui) {
                if (this.gui == null) this.connection.writeTitle(PacketSnapshots.EMPTY_TITLE);
                this.gui = this.newBuilder(extraLoginType);
                this.gui.show();
            }

            //else this.verificationMessage.updateMessage();//spoof for when the title is used

            this.loginVerification = new LoginVerification(data.getLoginParams().getExtraPassword(), false);
            this.duplexHandler.flush();
            return true;
        }
        return false;
    }

    //private static final PacketSnapshot TITLE =
    //private static final float INITIAL_YAW = 8.59e+8f;

    @Override
    public void sendInitial() {
        this.write(PacketSnapshots.PLAYER_ABILITIES_FLY);
        if (this.duplexHandler.isGeyser) this.write(BlockPackets.DECOY);
        //this.write(new PacketPlayerPositionAndLook(0.5, 64, 0.5, INITIAL_YAW, 0, 2));
        //this.write(new PacketPlayerPositionAndLook(0.5, 64, 0.5, 0, 0, 2));

        if (this.gui == null) {
            this.writeCommands();
            this.connection.writeTitle(this.isRegistered ? LOGIN_TITLE : REGISTER_TITLE);
        }

        if (this.gui != null) this.gui.show();
        else this.duplexHandler.flush();
        //this.write(PacketSnapshots.PACKET_PLAY_PLUGIN_MESSAGE);

        //Log.error("LOGIN SENT: " + this.gui + " NAMES: " + this.connection.getChannel().pipeline().names());
    }

    public void handleCommand(String[] args) {
        if (this.isRegistered) this.handleLoginCommand(args);
        else this.handleRegisterCommand(args);
    }

    private static final PacketSnapshot
            incorrectPasswordMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("incorrect-password")),
            incorrectPasswordKickPacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("incorrect-password"));

    public static final PacketSnapshot
            formatRegisterMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("format-register")),
            formatLoginMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("format-login")),
            registerPasswordsDoNotMatchMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("commands-register-passwords-do-not-match"));


    private void handleRegisterCommand(String[] args) {
        switch (args.length) {
            case 1: {//accept single inputs, even if repeat is explicitly enabled
                String password = args[0];
                this.registerIfValid(password, LoginType.COMMAND);
                return;
            }
            case 2: {
                String password = args[0];
                String arg2 = args[1];

                if (requirePasswordRepeatInRegister) {
                    if (!password.equals(arg2)) {
                        this.duplexHandler.writeAndFlush(registerPasswordsDoNotMatchMessagePacket);
                        return;
                    }
                } else {
                    this.duplexHandler.writeAndFlush(formatRegisterMessagePacket);
                    return;
                }

                this.registerIfValid(password, LoginType.COMMAND);
                return;
            }
            default: {
                this.duplexHandler.writeAndFlush(formatRegisterMessagePacket);
            }
        }
    }

    private void handleLoginCommand(String[] args) {
        if (args.length != 1) {
            this.duplexHandler.writeAndFlush(formatLoginMessagePacket);
            return;
        }

        String password = args[0]; //String.join("", args);

        if (this.isPasswordCorrect(password)) this.tryLogIn();
        else this.onIncorrectPassword();
    }

    @Override
    public void handle(PacketPlayInReconfigureAck packet) {
        if (!this.isAwaitingReconfigureAck) return;
        this.isAwaitingReconfigureAck = false;

        //we gotta wait because pe throws an exception when trying to read this packet (the server assumes config phase,
        // while the player sends this packet still in the play phase)
        AlixScheduler.async(this::logIn0);
    }

    @Override
    public void handle(PacketPlayInItemRename packet) {
        if (this.gui == null || !this.gui.isAnvil()) return;

        ((LimboAnvilBuilder) this.gui).updateText(packet.wrapper().getItemName());
    }

    @Override
    public void handle(PacketPlayInClickSlot packet) {
        if (this.gui != null) this.gui.select(packet.wrapper().getSlot());
    }

    @Override
    public void handle(PacketPlayInInventoryClose packet) {
        if (this.gui != null) this.gui.onCloseAttempt();
    }

    private long lastKeepAliveSentTime;
    private float lastYaw = 0;

    @Override
    public void handle(FlyingPacket packet) {
        long now = System.currentTimeMillis();
        long lastKeepAliveSent = now - lastKeepAliveSentTime;

        if (lastKeepAliveSent >= 15000) {
            this.writeAndFlush(KeepAlives.KEEP_ALIVE_PREVENT_TIMEOUT);
            this.lastKeepAliveSentTime = now;
        }

        var wrapper = packet.wrapper();

        if (wrapper.hasRotationChanged()) {
            var yaw = wrapper.getLocation().getYaw();
            float deltaYaw = Math.abs(yaw - this.lastYaw);

            /*var msg = "Yaw: " + yaw + " deltaYaw: " + deltaYaw;
            Log.error(msg);
            this.writeAndFlush(PacketPlayOutMessage.withMessage("§c" + msg));*/

            this.lastYaw = yaw;
        }
    }

    void disconnect(PacketOut disconnectPacket) {
        this.connection.sendPacketAndClose(disconnectPacket);
    }

    private void write(PacketOut packet) {
        this.duplexHandler.write(packet);
    }

    private void writeAndFlush(PacketOut packet) {
        this.duplexHandler.writeAndFlush(packet);
    }

    private Version version() {
        return this.connection.getClientVersion();
    }
}