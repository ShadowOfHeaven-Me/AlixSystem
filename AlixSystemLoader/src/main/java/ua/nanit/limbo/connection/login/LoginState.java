package ua.nanit.limbo.connection.login;

import alix.common.commands.file.CommandsFileManager;
import alix.common.data.AuthSetting;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.fingerprinting.FingerprintGateway;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.VerifiedCache;
import alix.common.data.security.email.EmailConfig;
import alix.common.data.security.email.EmailHandler;
import alix.common.data.security.email.recovery.EmailRecovery;
import alix.common.data.security.password.Password;
import alix.common.environment.ServerEnvironment;
import alix.common.login.LoginVerdict;
import alix.common.login.LoginVerification;
import alix.common.messages.Messages;
import alix.common.packets.command.CustomCommand;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.config.ConfigParams;
import alix.common.utils.floodgate.GeyserUtil;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ua.nanit.limbo.commands.LimboCommand;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.connection.captcha.blocks.BlockPackets;
import ua.nanit.limbo.connection.login.countdown.LimboCountdown;
import ua.nanit.limbo.connection.login.gui.*;
import ua.nanit.limbo.connection.login.gui.bedrock.LimboBedrockGUI;
import ua.nanit.limbo.connection.login.packets.SoundPackets;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;
import ua.nanit.limbo.protocol.packets.play.config.PacketPlayInReconfigureAck;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInClickSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInInventoryClose;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryClose;
import ua.nanit.limbo.protocol.packets.play.move.FlyingPacket;
import ua.nanit.limbo.protocol.packets.play.rename.PacketPlayInItemRename;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshots;
import ua.nanit.limbo.server.data.TitlePacketSnapshot;

import java.util.Arrays;
import java.util.function.Consumer;

import static alix.common.utils.config.ConfigProvider.config;
import static ua.nanit.limbo.connection.login.gui.LimboPinBuilder.maxLoginAttempts;
import static ua.nanit.limbo.protocol.snapshot.PacketSnapshots.EMAIL_VERIFY_TITLE;
import static ua.nanit.limbo.protocol.snapshot.PacketSnapshots.LOGIN_TITLE;
import static ua.nanit.limbo.protocol.snapshot.PacketSnapshots.REGISTER_TITLE;
import static ua.nanit.limbo.protocol.snapshot.PacketSnapshots.TERMS_TITLE;

public final class LoginState implements VerifyState {

    public static final boolean requirePasswordRepeatInRegister = config.getBoolean("require-password-repeat-in-register");
    //Whether registering players must additionally provide an email address, in the format /register <password> <email> (see the config for combining this with the password-repeat option above)
    public static final boolean requireEmailInRegister = config.getBoolean("require-email-in-register");
    //Whether registering players must first accept a set of Terms & Conditions (e.g. for GDPR/personal-data-collection compliance) before they're allowed to register
    public static final boolean requireTermsAcceptance = config.getBoolean("require-terms-acceptance");
    //The URL shown to players pointing at the Terms & Conditions they must accept, when the option above is enabled
    public static final String termsUrl = config.getString("terms-url", "");

    private static final PacketSnapshot
            REGISTER = createRegisterCommand(),
            LOGIN = createLimboCommand("login", Messages.get("commands-login-password-arg")),
            LOGIN_AND_RECOVERY = createMultiCommand(
                    CustomCommand.of("recovery", Messages.get("commands-recovery-email-arg")),
                    CustomCommand.of("login", Messages.get("commands-login-password-arg")));

    //The /register command hint shown to the client, matching whichever of 'require-password-repeat-in-
    //register'/'require-email-in-register' are enabled - when both are on, this now shows all 3 arguments
    //by their own real names (password, repeat password, email) via the general N-arg hint packet, rather
    //than the single best-effort combined label ("repeat password / email") a 2-arg-only hint packet used
    //to force this into.
    private static PacketSnapshot createRegisterCommand() {
        String password = Messages.get("commands-register-password-arg");
        if (requirePasswordRepeatInRegister && requireEmailInRegister)
            return createLimboCommand("register", password, Messages.get("commands-register-password-second-arg"), Messages.get("commands-register-email-arg"));
        if (requirePasswordRepeatInRegister)
            return createLimboCommand("register", password, Messages.get("commands-register-password-second-arg"));
        if (requireEmailInRegister)
            return createLimboCommand("register", password, Messages.get("commands-register-email-arg"));
        return createLimboCommand("register", password);
    }

    private static PacketSnapshot createMultiCommand(CustomCommand... commands) {
        return LimboCommand.constructMultiCommand(Arrays.asList(commands)).getPacketSnapshot();
    }

    private static PacketSnapshot createLimboCommand(String command, String arg1Name, String arg2Name, String arg3Name) {
        return LimboCommand.construct(CommandsFileManager.getCommand(command).getLabels(), arg1Name, arg2Name, arg3Name).getPacketSnapshot();
    }

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
    public LimboGUI originalGui;
    private Consumer<ClientConnection> authAction;
    private LoginVerification loginVerification;
    public PersistentUserData data;
    public boolean isRegistered;
    public int loginAttempts;
    //Whether this (still unregistered) connection has accepted the Terms & Conditions, when 'require-terms-acceptance' is enabled
    private boolean termsAccepted;
    //Set while a 'require-email-in-register' registration is waiting on the player to type "/verifyemail <code>" -
    //see handleRegisterCommandWithEmail()/handleRegisterVerifyEmailCommand(). Both are cleared together, so
    //null/non-null on the password field alone is enough to tell whether this gate is currently active.
    private String pendingRegisterPassword, pendingRegisterEmail;
    private int invalidRegisterCodeAttempts;

    public LoginState(ClientConnection connection) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
    }

    //Central choke point for EVERY registration flow (chat command, anvil GUI, PIN GUI, bedrock form) -
    //enforced here rather than only in the chat-command path, so no login type can register a new account
    //without first accepting the Terms & Conditions when 'require-terms-acceptance' is on. Callback-based
    //(rather than returning PersistentUserData directly) because the password validity check can involve a
    //real HaveIBeenPwned HTTP call - see AlixCommonUtils#getPasswordInvalidityReasonAsync - which must never
    //block whatever thread (typically a Netty event-loop thread) calls this.
    //
    //Guarantees the callback always runs on this connection's event loop, same as every caller could
    //already assume back when this was fully synchronous - callers write to duplexHandler/PacketDuplexHandler
    //from the callback all the time, and that asserts it's only ever touched from the event loop. Whenever
    //the HTTP call isn't needed (the overwhelming majority of calls), that's still the calling thread,
    //since it already had to be the event loop to reach here in the first place.
    public void registerIfValid(String password, LoginType type, Consumer<PersistentUserData> callback) {
        if (this.isTermsGateBlocking()) {
            this.sendTermsPrompt();
            callback.accept(null);
            return;
        }

        //can be optimized by creating PacketSnapshots for constant messages
        @OptimizationCandidate
        Consumer<String> onReason = reason -> this.runOnEventLoop(() -> {
            if (reason != null) {
                this.duplexHandler.write(SoundPackets.VILLAGER_NO);
                this.sendMessage(reason);
                callback.accept(null);
            } else {
                callback.accept(this.register0(password));
            }
        });
        AlixCommonUtils.getPasswordInvalidityReasonAsync(password, type, onReason);
    }

    //Runs r immediately if already on this connection's event loop, otherwise schedules it there - needed
    //whenever a callback (HaveIBeenPwned's HTTP check, FingerprintGateway) might complete on some other
    //thread (a virtual thread doing the actual network work) but still needs to touch
    //duplexHandler/PacketDuplexHandler afterward, which asserts it's only ever called from the event loop.
    private void runOnEventLoop(Runnable r) {
        var eventLoop = this.connection.getChannel().eventLoop();
        if (eventLoop.inEventLoop()) r.run();
        else eventLoop.execute(r);
    }

    private boolean isTermsGateBlocking() {
        return !this.isRegistered && requireTermsAcceptance && !this.termsAccepted;
    }

    //The title/subtitle shown above the hotbar should always reflect whatever step is actually required next -
    //previously it was picked solely off isRegistered, so an unregistered player still blocked by the Terms &
    //Conditions gate saw "Register with /register <password>" even though registering would still be refused
    //until they typed "/terms accept" first (that prompt only ever showed up in chat, via sendTermsPrompt()).
    private TitlePacketSnapshot currentTitle() {
        if (this.isTermsGateBlocking()) return TERMS_TITLE;
        if (this.isEmailRegisterGateBlocking()) return EMAIL_VERIFY_TITLE;
        return this.isRegistered ? LOGIN_TITLE : REGISTER_TITLE;
    }

    private PersistentUserData register0(String password) {
        if (this.data != null) this.data.setPassword(password);
        else {
            this.data = PersistentUserData.createDefault(this.connection.getUsername(),
                    this.connection.getAddress(), Password.fromUnhashed(password));
            if (VerifiedCache.getAndCheckIfEquals(this.connection.getUsername(), this.connection.getChannel()))
                this.data.setPremiumData(PremiumDataCache.getOrUnknown(this.connection.getUsername()));
        }

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

        if (ServerEnvironment.isVelocity() && this.connection.hasConfigPhase()) {
            this.duplexHandler.writeAndFlush(PacketSnapshots.RECONFIGURE);
            this.duplexHandler.disablePacketWriting = true;
            this.isAwaitingReconfigureAck = true;
            return;
        }

        AlixScheduler.async(this::logIn0);
    }

    private void logIn0() {
        this.data.setIP(this.connection.getAddress());
        var config = this.connection.getChannel().config();

        //disallow any reads after uninjecting the handlers, but before the authAction happens
        config.setAutoRead(false);

        this.connection.uninjectConnection();
        this.authAction.accept(this.connection);

        config.setAutoRead(true);
    }

    public void writeMessage(String s) {
        this.duplexHandler.write(PacketPlayOutMessage.withMessage(s));
    }

    void sendMessage(String s) {
        this.duplexHandler.writeAndFlush(PacketPlayOutMessage.withMessage(s));
    }

    public boolean isPasswordCorrect(String password) {
        //loginVerification is only created for password-based accounts (see setData()) - an
        //authenticator-app-only ("AUTH_APP") account has none, so guard against it defensively rather
        //than risk an NPE if a password check is ever attempted for one.
        return this.loginVerification != null && this.loginVerification.isPasswordCorrect(password);
    }

    //true if the connection will stay alive (the user wasn't kicked)
    public boolean onIncorrectPassword() {
        //FUNCTIONALITY (audit, 2026-09-24): >= not == - loginAttempts only ever counts up from 0, so a
        //config value of 0 or negative (an admin applying this file's own "0 or less = disable" convention,
        //documented right next to this on a different setting) meant == could never match and the lockout
        //never fired at all, letting an attacker guess passwords without limit.
        if (++loginAttempts >= maxLoginAttempts) {
            this.connection.sendPacketAndClose(incorrectPasswordKickPacket);
            return false;
        }

        this.write(SoundPackets.VILLAGER_NO);
        this.writeAndFlush(incorrectPasswordMessagePacket);

        if (this.loginAttempts == 2 && this.data.canUseEmailRecovery())
            this.sendMessage(EmailRecovery.recoveryReminder(this.data));

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
        //Told once, in chat, rather than as a GUI button (unlike the email/passkey "Recover account?" item,
        //which LimboAuthBuilder only shows when canUseAnyRecovery() - recovery codes are generated
        //automatically alongside the token itself, see PersistentUserData#regenerateAuthToken(), so there's
        //no equivalent "has this been set up at all" check to gate a button on without an async DB call).
        this.sendMessage(Messages.getWithPrefix("recovery-code-hint"));

        return new LimboAuthBuilder(this.connection, this.data, correct -> {
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
        //Must also cover plain AUTH_APP (app-only, no password), not just PASSWORD_AND_AUTH_APP - normal
        //login for an AUTH_APP-only account never reaches this (setData() shows the 2FA GUI directly and
        //skips tryLogIn() for that path), but every recovery success handler (handleRecoveryCommand,
        //handleRecoveryPasskeyCommand, LimboRecoveryAnvilBuilder, LimboPinBuilder) calls tryLogIn() ->
        //init2FA() - proving only email/passkey ownership there must still require this account's actual
        //(and, for AUTH_APP, ONLY) 2FA factor before logging in, or recovery silently bypasses 2FA entirely.
        var authSettings = this.data.getLoginParams().getAuthSettings();
        if (authSettings != AuthSetting.PASSWORD_AND_AUTH_APP && authSettings != AuthSetting.AUTH_APP) return false;
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
        /*if (PacketPlayOutShowDialog.write(this.connection))
            return;*/

        if (this.version().moreOrEqual(Version.V1_13)) {
            if (this.data != null && this.data.canUseEmailRecovery()) {
                this.write(LOGIN_AND_RECOVERY);
                return;
            }

            this.write(this.isRegistered ? LOGIN : REGISTER);
        }
    }

    private boolean initDoubleVer() {
        //loginVerification is null for an AUTH_APP-only account (see setData()'s justAuthApp branch) - guard
        //defensively rather than NPE if double-verification is ever configured on one (nothing currently
        //prevents that combination), same reasoning as isPasswordCorrect()'s own guard above.
        if (loginVerification != null && data.getLoginParams().isDoubleVerificationEnabled() && loginVerification.isPhase1()) {
            var extraLoginType = data.getLoginParams().getExtraLoginType();
            var isSecondaryGui = extraLoginType != LoginType.COMMAND;

            if (this.gui != null && !isSecondaryGui) {
                this.write(CLOSE_INV);
                this.writeCommands();
                this.connection.writeTitle(this.currentTitle());
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
        if (this.duplexHandler.isGeyser) this.write(BlockPackets.DECOY);//so the mf doesn't fall into the void
        //this.write(new PacketPlayerPositionAndLook(0.5, 64, 0.5, INITIAL_YAW, 0, 2));
        //this.write(new PacketPlayerPositionAndLook(0.5, 64, 0.5, 0, 0, 2));

        if (this.gui == null) {
            this.writeCommands();
            this.connection.writeTitle(this.currentTitle());
        }
        //DO NOT SEND THIS, BREAKS GUIS
        //this.write(Entities.SAME_ID);

        if (this.gui != null) this.gui.show();
        else this.duplexHandler.flush();
        //this.write(PacketSnapshots.PACKET_PLAY_PLUGIN_MESSAGE);

        //Log.error("LOGIN SENT: " + this.gui + " NAMES: " + this.connection.getChannel().pipeline().names());

        //Prompt unregistered players to accept the Terms & Conditions before they're allowed to register.
        //Sent via chat regardless of whether a login GUI (anvil/PIN/bedrock) is also showing, since chat
        //messages are still delivered while such a GUI is open, and registerIfValid() enforces the actual
        //gate for every login type - so GUI-only players still need to see this to know why registering
        //isn't working yet and that they must type "/terms accept" in chat first.
        if (this.isTermsGateBlocking())
            this.sendTermsPrompt();
    }

    public void handleCommand(String rawCmd) {
        if (rawCmd == null || rawCmd.isEmpty()) return;
        if (rawCmd.charAt(0) == '/') rawCmd = rawCmd.substring(1);
        String[] split = rawCmd.split(" ");
        String cmdName = split[0].toLowerCase();
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        //While a login GUI (anvil/PIN/bedrock/2FA/recovery) is showing, the only commands still allowed
        //through chat are "recovery"/"recoveremail"/"recoverpasskey"/"recoverycode" (to start/continue
        //account recovery), "terms" (to accept/decline the Terms & Conditions, since that's chat-only and
        //has no GUI of its own) and "verifyemail" (to complete a 'require-email-in-register' registration,
        //also chat-only) - everything else must go through the GUI itself (this is also what stops mods
        //from automatically driving arbitrary register commands through chat while a PIN GUI is showing).
        //This is the ONLY gate on GUI-open command handling (audit, 2026-09-24: a now-removed, cruder
        //"any command at all while gui != null" check used to sit above this and return unconditionally
        //before this allowlist ever ran - meaning recovery/terms/verifyemail were silently unreachable
        //through the exact GUI states they're documented to work during, e.g. a lost-device player
        //standing at the 2FA prompt had no way to ever redeem a recovery code). Enforced once here so it
        //applies uniformly to every command source: signed and unsigned 1.19+ command packets and legacy
        //pre-1.19 chat-as-command alike.
        if (this.gui != null && !cmdName.equals("recovery") && !cmdName.equals("recoveremail")
                && !cmdName.equals("recoverpasskey") && !cmdName.equals("recoverycode")
                && !cmdName.equals("terms") && !cmdName.equals("verifyemail"))
            return;

        if (cmdName.equals("recovery")) {
            this.handleRecoveryCommand(args);
            return;
        }

        //"I lost my authenticator app/device" - a backup code takes the place of the 6-digit TOTP code
        //specifically (see PersistentUserData#tryConsumeRecoveryCode()), not the password: unlike
        //"recovery"/"recoveremail" above (which recover a forgotten PASSWORD), this only ever makes sense
        //for an account that currently requires the auth app at all (AUTH_APP or PASSWORD_AND_AUTH_APP) -
        //handleRecoveryCodeCommand() itself guards against being used otherwise.
        if (cmdName.equals("recoverycode")) {
            this.handleRecoveryCodeCommand(args);
            return;
        }

        //Both also reachable via LimboRecoveryChoiceBuilder's buttons when an account has both email and
        //passkey recovery set up (see openRecovery()) - kept as chat commands too as an always-available
        //fallback, same as "/recovery" itself.
        if (cmdName.equals("recoveremail")) {
            this.openRecoveryEmailGui();
            return;
        }

        if (cmdName.equals("recoverpasskey")) {
            this.handleRecoveryPasskeyCommand();
            return;
        }

        //Dispatched before the isRegistered check below (like "recovery" above), even though "terms" is only
        //ever meaningful for an unregistered account - handleTermsCommand() itself now no-ops for a registered
        //one. Previously this branch sat AFTER the isRegistered check, so a registered player typing
        //"/terms accept"/"/terms decline" while their login GUI was open (allowed through by the gate above)
        //fell into handleLoginCommand() instead, silently treating "accept"/"decline" as a wrong password guess
        //and burning one of their limited login attempts.
        if (cmdName.equals("terms")) {
            this.handleTermsCommand(args);
            return;
        }

        //Same reasoning as "terms" above: must be dispatched before the isRegistered/gate checks below, since
        //this is exactly how an unregistered, gated connection is expected to eventually become registered.
        if (cmdName.equals("verifyemail")) {
            this.handleRegisterVerifyEmailCommand(args);
            return;
        }

        if (this.isRegistered) {
            this.handleLoginCommand(args);
            return;
        }

        if (this.isTermsGateBlocking()) {
            this.sendTermsRequiredReminder();
            return;
        }

        if (this.isEmailRegisterGateBlocking()) {
            this.sendEmailRegisterGatePrompt();
            return;
        }

        this.handleRegisterCommand(args);
    }

    //Sends the Terms & Conditions prompt (explanation + link + instructions) to an unregistered player
    private void sendTermsPrompt() {
        this.writeMessage(Messages.getWithPrefix("terms-required-explanation"));
        this.duplexHandler.write(PacketPlayOutMessage.withComponent(buildTermsLinkComponent()));
        this.sendMessage(Messages.getWithPrefix("terms-required-prompt"));
    }

    //Reminds a gated-but-unregistered player that they still need to accept the Terms & Conditions before
    //anything else they type (other than "/terms accept"/"/terms decline" itself) will do anything - a
    //short nag rather than the full explanation+link+prompt of sendTermsPrompt() above (which already ran
    //once on join), mirroring sendEmailRegisterGatePrompt()'s role for the email-verification gate.
    private void sendTermsRequiredReminder() {
        this.sendMessage(Messages.getWithPrefix("terms-must-accept-first"));
    }

    //Builds the "terms-required-link" line as a real clickable/hoverable link (opens directly in the
    //player's browser when clicked) instead of inert plain text - previously sent via writeMessage() like
    //every other message here, but the legacy '&'/'§'-coded string format that goes through has no way to
    //attach a click event, so the URL was never actually clickable no matter how it was styled/colored.
    //The lang key's raw template (still holding its unsubstituted "{0}" placeholder) is split around that
    //placeholder so the surrounding legacy-formatted text is preserved exactly, with only the URL itself
    //replaced by the clickable component.
    private Component buildTermsLinkComponent() {
        String template = AlixFormatter.appendPrefix(Messages.get("terms-required-link"));
        String[] parts = template.split("\\{0\\}", 2);

        Component link = Component.text(termsUrl)
                .clickEvent(ClickEvent.openUrl(termsUrl))
                .hoverEvent(HoverEvent.showText(Component.text(termsUrl)))
                .decorate(TextDecoration.UNDERLINED);

        Component result = LegacyComponentSerializer.legacySection().deserialize(parts[0]).append(link);
        if (parts.length > 1 && !parts[1].isEmpty())
            result = result.append(LegacyComponentSerializer.legacySection().deserialize(parts[1]));
        return result;
    }

    //Handles the pre-login '/terms accept' and '/terms decline' commands, used to gate registration behind Terms & Conditions acceptance
    private void handleTermsCommand(String[] args) {
        //Only meaningful for an unregistered account - a registered one has nothing to accept/decline, so
        //just ignore it rather than falling through to any register/login-flow behavior. (This is reachable
        //now that "terms" is dispatched before the isRegistered check above, mirroring "recovery".)
        if (this.isRegistered) return;

        //If 'require-terms-acceptance' is off, this command has nothing to gate - previously it would still
        //flip the internal (unused, in that case) termsAccepted flag and immediately show the "Format:
        ///register ..." hint regardless, which looked like this command "did something" (and, taken together
        //with registration then succeeding normally since the gate was never active to begin with, could be
        //misread as terms acceptance being silently bypassed) even though the feature is entirely disabled.
        //Telling the player plainly it isn't required avoids that confusion.
        if (!requireTermsAcceptance) {
            this.sendMessage(Messages.getWithPrefix("terms-not-required"));
            return;
        }

        if (args.length != 1) {
            this.sendMessage(Messages.getWithPrefix("terms-invalid-input"));
            return;
        }

        String choice = args[0].toLowerCase();

        if (choice.equals("accept")) {
            this.termsAccepted = true;
            //Previously only the "Format: /register ..." hint was sent here, with nothing actually
            //confirming the acceptance itself - unlike "/terms decline" (which kicks with a clear reason),
            //a player typing "/terms accept" had no visible confirmation that anything happened at all.
            this.writeMessage(Messages.getWithPrefix("terms-accepted"));
            //Swap the hotbar title from "Accept the terms with /terms accept" back to the normal register
            //prompt now that the gate is actually cleared - see currentTitle()/isTermsGateBlocking(). Guarded
            //the same way sendInitial()/initDoubleVer() already guard their own title writes, since a title
            //packet isn't meaningful while a login GUI (anvil/PIN/bedrock) is covering it instead.
            if (this.gui == null) this.connection.writeTitle(this.currentTitle());
            this.duplexHandler.writeAndFlush(requireEmailInRegister ? formatRegisterEmailMessagePacket : formatRegisterMessagePacket);
            return;
        }

        if (choice.equals("decline")) {
            this.disconnect(termsDeclinedKickPacket);
            return;
        }

        this.sendMessage(Messages.getWithPrefix("terms-invalid-input"));
    }

    private static final int
            MAX_EMAIL_ATTEMPTS = EmailConfig.getConfig().getInt("max-email-attempts"),
            MAX_CODE_ATTEMPTS = EmailConfig.getConfig().getInt("max-code-attempts");
    private int invalidEmailAttempts, invalidCodeAttempts, invalidRecoveryCodeAttempts;

    public void onInvalidCode() {

    }

    //Shared by the chat "/recovery" path (handleRecoveryCommand below) and the anvil-GUI recovery flow
    //(LimboRecoveryAnvilBuilder, a different package - hence public) so the two can never drift apart on
    //attempt-limit enforcement again: the GUI path previously called EmailHandler directly and never
    //touched these counters at all, letting a player brute-force max-email-attempts/max-code-attempts
    //(email-config.yml) indefinitely through the GUI while the chat path correctly enforced them.
    //Returns true if the limit was hit (and the connection was disconnected) - callers must stop immediately.
    public boolean registerInvalidRecoveryEmailAttempt() {
        if (++this.invalidEmailAttempts >= MAX_EMAIL_ATTEMPTS) {
            this.disconnect(PacketPlayOutDisconnect.of(Messages.getWithPrefix("email-recovery-invalid-email")));
            return true;
        }
        return false;
    }

    public boolean registerInvalidRecoveryCodeAttempt() {
        if (++this.invalidCodeAttempts >= MAX_CODE_ATTEMPTS) {
            this.disconnect(PacketPlayOutDisconnect.of(Messages.getWithPrefix("email-recovery-invalid-email")));
            return true;
        }
        return false;
    }

    private void handleRecoveryCommand(String[] args) {
        if (args.length != 1) {
            this.sendMessage(Messages.get("email-recovery-invalid-email"));
            return;
        }

        String input = args[0].trim();
        if (EmailHandler.hasSession(this.connection)) {
            if (EmailHandler.verifyRecoveryCode(this.connection, input)) {
                this.sendMessage(Messages.getWithPrefix("email-recovery-success"));
                this.tryLogIn();
                return;
            }
            if (this.registerInvalidRecoveryCodeAttempt()) return;
            this.sendMessage(Messages.getWithPrefix("email-recovery-invalid-email"));
            return;
        }

        if (this.data == null || this.data.getEmail() == null) {
            this.sendMessage(Messages.getWithPrefix("email-recovery-no-email"));
            return;
        }

        String registeredEmail = this.data.getEmail().email();
        if (input.equalsIgnoreCase(registeredEmail)) {
            EmailHandler.sendRecoveryMail(this.connection, input, (conn, msg) -> this.sendMessage(msg));
            this.openRecoveryCodeGui();
        } else {
            if (this.registerInvalidRecoveryEmailAttempt()) return;
            this.sendMessage(Messages.getWithPrefix("email-recovery-invalid-email"));
        }
    }

    //"/recoverycode <code>" - consumes one of this account's Google Authenticator backup codes in place of
    //the 6-digit TOTP code, for a player who still has their password but has lost the device/app that
    //generates it. Deliberately NOT routed through openRecovery()/tryLogIn() like the email/passkey
    //recovery methods above: those recover a forgotten PASSWORD and still leave 2FA (a separate factor) in
    //effect afterward (tryLogIn() -> init2FA()) - here the code IS the 2FA factor itself, so re-running
    //init2FA() after a successful match would just show the same GUI again and strand the player. Only
    //initDoubleVer() (an unrelated secondary-password step) still applies.
    //
    //SECURITY: a recovery code must NEVER be accepted as a substitute for the PASSWORD on a
    //PASSWORD_AND_AUTH_APP account - it only ever stands in for the app/TOTP factor. The command-dispatch
    //gate in handleCommand() lets "recoverycode" through at ANY point while a login GUI is showing,
    //including the very first (password) GUI, before any password has been checked - so this method itself
    //must verify the password step already happened, or a leaked/guessed single recovery code would let an
    //attacker skip the password entirely on a two-factor account. this.gui is only ever the 2FA
    //(LimboAuthBuilder) GUI for such an account after init2FA() - which is only ever reached via
    //tryLogIn(), itself only ever reached after a correct password (or a fresh registration) - so "we are
    //currently showing the 2FA GUI" is exactly "the password step already succeeded". An AUTH_APP-only
    //account has no password to bypass in the first place, so this check doesn't apply to it.
    private void handleRecoveryCodeCommand(String[] args) {
        if (this.data == null || args.length != 1) {
            this.sendMessage(Messages.getWithPrefix("recovery-code-invalid-input"));
            return;
        }

        var authSettings = this.data.getLoginParams().getAuthSettings();
        if (authSettings != AuthSetting.AUTH_APP && authSettings != AuthSetting.PASSWORD_AND_AUTH_APP) {
            this.sendMessage(Messages.getWithPrefix("recovery-code-not-applicable"));
            return;
        }

        if (authSettings == AuthSetting.PASSWORD_AND_AUTH_APP && !(this.gui instanceof LimboAuthBuilder)) {
            this.sendMessage(Messages.getWithPrefix("recovery-code-password-required"));
            return;
        }

        this.data.tryConsumeRecoveryCode(args[0], matched -> this.runOnEventLoop(() -> {
            if (!matched) {
                //Same attempt-cap treatment every other guessable code in this class already gets
                //(email-recovery code, register-email-verify code) - unguessable given the keyspace alone,
                //but there's no reason this specific guess loop should be the one exception left unbounded.
                if (++this.invalidRecoveryCodeAttempts >= MAX_CODE_ATTEMPTS) {
                    this.disconnect(PacketPlayOutDisconnect.of(Messages.getWithPrefix("recovery-code-invalid")));
                    return;
                }
                this.sendMessage(Messages.getWithPrefix("recovery-code-invalid"));
                return;
            }

            this.sendMessage(Messages.getWithPrefix("recovery-code-success"));
            //A correctly-typed recovery code is at least as strong a proof of device/account ownership as
            //a live TOTP code - marking it here lets a player who reset their 2FA in-game (see
            //shadow.utils.objects.savable.data.AuthDataChanges / the Velocity port, both now gated behind
            //hasProvenAuthAccess the same way changing the auth TYPE already was) actually get past that
            //gate on their very next in-game visit, even though they have no working TOTP code to re-enter
            //there - which is exactly the situation that led them to use a recovery code to log in in the
            //first place. Without this, a player who genuinely lost their device would be stuck unable to
            //ever regenerate it in-game.
            this.data.getLoginParams().setHasProvenAuthAccess(true);
            if (this.initDoubleVer()) return;
            this.logIn();
        }));
    }

    //Entry point for the "Recover account?" item (LimboAnvilBuilder/LimboPinBuilder/the bedrock form) -
    //dispatches to whichever recovery method(s) this account actually has available. When both email and a
    //device passkey are set up, opens LimboRecoveryChoiceBuilder to let the player pick which one to use -
    //previously this only sent a clickable chat message instead of a real GUI, which Shadow asked to have
    //changed.
    public void openRecovery() {
        boolean canEmail = this.data != null && this.data.canUseEmailRecovery();
        boolean canPasskey = this.data != null && this.data.canUsePasskeyRecovery();

        if (canEmail && canPasskey) {
            this.openRecoveryChoiceGui();
        } else if (canEmail) {
            this.openRecoveryEmailGui();
        } else if (canPasskey) {
            this.handleRecoveryPasskeyCommand();
        }
        //else: this item shouldn't be shown at all when neither is available - see canUseAnyRecovery()
    }

    //"/recoverpasskey" - the passkey counterpart to "/recovery <email/code>" (handleRecoveryCommand()),
    //also reachable directly from openRecovery() above when a passkey is this account's only recovery
    //option, or via LimboRecoveryChoiceBuilder's passkey button when both methods are available. Public
    //(rather than the original private) since LimboRecoveryChoiceBuilder, in the "gui" sub-package, needs
    //to call it too.
    public void handleRecoveryPasskeyCommand() {
        if (this.data == null || !this.data.canUsePasskeyRecovery()) {
            this.sendMessage(Messages.getWithPrefix("device-fingerprint-mismatch"));
            return;
        }

        FingerprintGateway.sendFingerprintingPacks(this.connection.getChannel(), correct -> this.runOnEventLoop(() -> {
            if (correct) {
                this.sendMessage(Messages.getWithPrefix("email-recovery-success"));
                this.tryLogIn();
            } else {
                this.sendMessage(Messages.getWithPrefix("device-fingerprint-mismatch"));
            }
        }));
    }

    public void openRecoveryChoiceGui() {
        if (this.gui != null && !this.isRecoveryGui(this.gui)) {
            this.originalGui = this.gui;
        }
        this.gui = new LimboRecoveryChoiceBuilder(this.connection, this);
        this.gui.show();
    }

    public void openRecoveryEmailGui() {
        if (this.gui != null && !this.isRecoveryGui(this.gui)) {
            this.originalGui = this.gui;
        }
        this.gui = new LimboRecoveryAnvilBuilder(this.connection, this, false);
        this.gui.show();
    }

    public void openRecoveryCodeGui() {
        if (this.gui != null && !this.isRecoveryGui(this.gui)) {
            this.originalGui = this.gui;
        }
        this.gui = new LimboRecoveryAnvilBuilder(this.connection, this, true);
        this.gui.show();
    }

    //True for any GUI belonging to the recovery flow itself (the initial email/passkey choice, and the
    //email/code anvil steps) - used by the openRecovery*Gui() methods above so that transitioning between
    //these doesn't clobber originalGui (the login/PIN/anvil GUI the player was on before starting recovery,
    //which reopenOriginalGui() returns to on cancel).
    private boolean isRecoveryGui(LimboGUI gui) {
        return gui instanceof LimboRecoveryAnvilBuilder || gui instanceof LimboRecoveryChoiceBuilder;
    }

    public void reopenOriginalGui() {
        if (this.originalGui != null) {
            this.gui = this.originalGui;
            this.gui.show();
        } else if (this.data != null) {
            var loginType = this.data.getLoginType();
            this.gui = this.newBuilder(loginType);
            this.gui.show();
        }
    }

    public static final PacketSnapshot
            incorrectPasswordMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("incorrect-password")),
            incorrectPasswordKickPacket = PacketPlayOutDisconnect.snapshot(Messages.getWithPrefix("incorrect-password")),
            //Must reflect require-password-repeat-in-register too, the same way createRegisterCommand()'s
            //client-side hint and LimboConfig's register title already do - previously these two were always
            //built from the plain (non-repeat) key regardless of that setting, so a misformatted /register
            //(e.g. only one password when a repeat is required) showed a "Format: /register <password>" hint
            //missing the second <password> argument the server was actually about to require.
            formatRegisterMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix(requirePasswordRepeatInRegister ? "format-register-repeat" : "format-register")),
            formatRegisterEmailMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix(requirePasswordRepeatInRegister ? "format-register-repeat-email" : "format-register-email")),
            formatLoginMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("format-login")),
            registerPasswordsDoNotMatchMessagePacket = PacketPlayOutMessage.snapshot(Messages.getWithPrefix("commands-register-passwords-do-not-match")),
            termsDeclinedKickPacket = PacketPlayOutDisconnect.snapshot(Messages.getWithPrefix("terms-declined-kick"));

    private void handleRegisterCommand(String[] args) {
        if (requireEmailInRegister) {
            this.handleRegisterCommandWithEmail(args);
            return;
        }

        switch (args.length) {
            //todo: reconsider
            case 1: {//accept single inputs, even if repeat is explicitly enabled
                String password = args[0];
                this.registerIfValid(password, LoginType.COMMAND, AlixCommonUtils.EMPTY_CONSUMER);
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

                this.registerIfValid(password, LoginType.COMMAND, AlixCommonUtils.EMPTY_CONSUMER);
                return;
            }
            default: {
                this.duplexHandler.writeAndFlush(formatRegisterMessagePacket);
            }
        }
    }

    //Handles /register when a mandatory email is configured ('require-email-in-register'). Expected format is either
    // /register <password> <email>, or /register <password> <password> <email> when password-repeat is also enabled.
    //
    //The account is deliberately NOT created here - only once the code sent below is confirmed via "/verifyemail
    //<code>" (see handleRegisterVerifyEmailCommand()). Earlier revisions called registerIfValid() immediately and
    //saved the (unverified) email straight onto the new account, kicking off verification only after the player
    //had already fully joined - which meant an account with a never-verified email was fully playable, and every
    //other Alix-side or external (e.g. a website integration reading alix_users2.email) consumer of that column
    //had no way to tell it apart from a genuinely verified one, since nothing else in Alix ever puts an
    //unverified value there. Gating registration itself on the code instead - the same pattern
    //'require-terms-acceptance' already uses for Terms & Conditions - closes that gap entirely: if the player
    //never verifies, they simply never end up with an account at all (nothing is persisted), rather than ending
    //up with one whose email can't be trusted.
    private void handleRegisterCommandWithEmail(String[] args) {
        int expectedArgs = requirePasswordRepeatInRegister ? 3 : 2;
        if (args.length != expectedArgs) {
            this.duplexHandler.writeAndFlush(formatRegisterEmailMessagePacket);
            return;
        }

        String password = args[0];

        if (requirePasswordRepeatInRegister) {
            String repeat = args[1];
            if (!password.equals(repeat)) {
                this.duplexHandler.writeAndFlush(registerPasswordsDoNotMatchMessagePacket);
                return;
            }
        }

        String email = args[args.length - 1];
        if (!EmailHandler.isValidEmail(email)) {
            this.sendMessage(Messages.getWithPrefix("verify-mail.invalid-email"));
            return;
        }

        //Validate the password itself up front, the same way registerIfValid() eventually will - there's no
        //PersistentUserData to run that check against yet (the account doesn't exist until the code below is
        //confirmed), and there's no reason to burn a real email send on a password that's going to be rejected
        //anyway.
        AlixCommonUtils.getPasswordInvalidityReasonAsync(password, LoginType.COMMAND, reason -> this.runOnEventLoop(() -> {
            if (reason != null) {
                this.duplexHandler.write(SoundPackets.VILLAGER_NO);
                this.sendMessage(reason);
                return;
            }

            this.pendingRegisterPassword = password;
            this.pendingRegisterEmail = email;
            //Swap the hotbar title to "Verify your email with /verifyemail <code>" now that the gate is
            //active - see currentTitle()/isEmailRegisterGateBlocking(). Guarded the same way sendInitial()/
            //initDoubleVer() already guard their own title writes, since a title packet isn't meaningful
            //while a login GUI (anvil/PIN/bedrock) is covering it instead.
            if (this.gui == null) this.connection.writeTitle(this.currentTitle());
            //Includes a clickable web-verification link (if 'enable-web-verification' is on) alongside the
            //6-digit code - clicking it completes this exact same pending registration, gated behind the same
            //single-use code check "/verifyemail <code>" itself goes through (see
            //EmailHandler#sendVerifyMailForPendingRegistration and completeEmailRegistration()'s own guard for
            //why running this from the web server's own thread, possibly racing a concurrent chat-typed code,
            //is still safe).
            EmailHandler.sendVerifyMailForPendingRegistration(this.connection, email, (conn, msg) -> this.sendMessage(msg),
                    () -> this.runOnEventLoop(this::completeEmailRegistration));
            this.sendMessage(Messages.getWithPrefix("register-email-verification-sent", email));

            //Switch from the general max-login-time countdown to the (typically longer) dedicated
            //email-verification one - see ConfigParams#emailVerificationTime for why: receiving the email
            //can easily take longer than max-login-time allows for the rest of the register GUI, and without
            //this the player could get kicked (losing pendingRegisterPassword/Email, since the account isn't
            //created yet) before the code even arrives, forcing them to restart registration and wait for a
            //new email every time.
            if (ConfigParams.hasEmailVerificationTime) {
                if (this.countdown != null) this.countdown.cancel();
                this.countdown = new LimboCountdown(this.connection);
            }
        }));
    }

    //True while a 'require-email-in-register' registration is waiting on the player to confirm their email via
    //"/verifyemail <code>" - see handleRegisterCommandWithEmail().
    private boolean isEmailRegisterGateBlocking() {
        return this.pendingRegisterPassword != null;
    }

    //Reminds a gated-but-unregistered player that they still need to confirm their email before anything else
    //they type (other than "/verifyemail <code>" itself) will do anything - mirrors sendTermsPrompt()'s role for
    //the Terms & Conditions gate.
    private void sendEmailRegisterGatePrompt() {
        this.sendMessage(Messages.getWithPrefix("register-email-verification-required"));
    }

    //Handles the pre-login "/verifyemail <code>" command that completes a 'require-email-in-register'
    //registration - see handleRegisterCommandWithEmail() for why the account isn't created until this succeeds.
    private void handleRegisterVerifyEmailCommand(String[] args) {
        if (!this.isEmailRegisterGateBlocking()) {
            this.sendMessage(Messages.getWithPrefix("verify-mail.send-first", "/register"));
            return;
        }

        if (args.length != 1) {
            this.sendMessage(Messages.getWithPrefix("register-email-verification-required"));
            return;
        }

        if (EmailHandler.verifyCode(this.connection, args[0].trim())) {
            this.completeEmailRegistration();
            return;
        }

        if (++this.invalidRegisterCodeAttempts >= MAX_CODE_ATTEMPTS) {
            this.disconnect(PacketPlayOutDisconnect.of(Messages.getWithPrefix("register-email-verification-too-many-attempts")));
            return;
        }

        this.sendMessage(Messages.getWithPrefix("verify-mail.code-mismatch"));
    }

    //Finishes a 'require-email-in-register' registration once its code has been confirmed - either typed in
    //chat (handleRegisterVerifyEmailCommand() above) or via the clickable web-verification link (see
    //handleRegisterCommandWithEmail()'s onRegisterVerified callback). Must run on this connection's event
    //loop (both callers already guarantee that - the chat path natively, the web link via runOnEventLoop()).
    //EmailHandler#verifyCode()'s atomic computeIfPresent() already guarantees at most one of those two paths
    //ever gets a true result for the same code, so in practice only one of them ever reaches here - the
    //isEmailRegisterGateBlocking() guard just makes that an explicit, cheap invariant of this method itself
    //(reachable from two call sites) rather than something callers have to trust holds elsewhere.
    private void completeEmailRegistration() {
        if (!this.isEmailRegisterGateBlocking()) return;

        String password = this.pendingRegisterPassword;
        String email = this.pendingRegisterEmail;
        this.pendingRegisterPassword = null;
        this.pendingRegisterEmail = null;

        //registerIfValid() re-validates the password and re-checks the terms gate - both already known-good
        //by this point, but going through the same central choke point as every other registration path
        //rather than duplicating (or bypassing) its checks is worth the redundant work.
        this.registerIfValid(password, LoginType.COMMAND, registered -> {
            if (registered != null) registered.setEmail(email);
        });
    }

    private void handleLoginCommand(String[] args) {
        if (args.length != 1) {
            this.duplexHandler.writeAndFlush(formatLoginMessagePacket);
            return;
        }

        String password = args[0]; //String.join("", args);

        if (this.isPasswordCorrect(password))
            this.tryLogIn();
        else
            this.onIncorrectPassword();
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

        ((AbstractAnvilBuilder<?>) this.gui).updateText(packet.wrapper().getItemName());
    }

    @Override
    public void handle(PacketPlayInClickSlot packet) {
        if (this.gui != null) this.gui.select(packet.wrapper().getSlot());
    }

    @Override
    public void handle(PacketPlayInInventoryClose packet) {
        if (this.gui != null) this.gui.onCloseAttempt();
    }

    //private long lastKeepAliveSentTime;
    //private float lastYaw = 0;

    @Override
    public void handle(FlyingPacket packet) {
        /*long now = System.currentTimeMillis();
        long lastKeepAliveSent = now - lastKeepAliveSentTime;

        if (lastKeepAliveSent >= 10000) {
            this.write(KeepAlives.KEEP_ALIVE_PREVENT_TIMEOUT);
            this.lastKeepAliveSentTime = now;
        }*/

        /*var wrapper = packet.wrapper();

        if (wrapper.hasRotationChanged()) {
            var yaw = wrapper.getLocation().getYaw();
            float deltaYaw = Math.abs(yaw - this.lastYaw);

            *//*var msg = "Yaw: " + yaw + " deltaYaw: " + deltaYaw;
            Log.error(msg);
            this.write(PacketPlayOutMessage.withMessage("§c" + msg));*//*

            this.lastYaw = yaw;
        }*/
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