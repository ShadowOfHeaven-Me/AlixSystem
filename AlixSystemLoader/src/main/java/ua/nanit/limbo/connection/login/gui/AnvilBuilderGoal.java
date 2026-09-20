package ua.nanit.limbo.connection.login.gui;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixError;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryOpen;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

public enum AnvilBuilderGoal {
    REGISTER,
    //REGISTER_PIN,
    LOGIN,
    CHANGE_PASSWORD,
    CHANGE_PIN,
    RECOVERY_EMAIL,
    RECOVERY_CODE;

    //These titles are the actual anvil inventory window titles shown to the player, and previously bypassed
    //the Messages/i18n system entirely with hardcoded English literals (unlike anvilInvOpenRecoveryEmail/Code
    //just below, which were already correctly routed through Messages.get) - this was the root cause of the
    //"untranslated text in the Change Password GUI" report (and affected the Login/Register/Change PIN anvil
    //titles the exact same way, just not yet noticed/reported).
    private static final PacketSnapshot
            anvilInvOpenLogin = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.ANVIL, Messages.get("gui-title-login")),
            anvilInvOpenRegister = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.ANVIL, Messages.get("gui-title-register")),
            anvilInvOpenPasswordChange = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.ANVIL, Messages.get("anvil-title-change-password")),
            anvilInvOpenPinChange = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.ANVIL, Messages.get("anvil-title-change-pin")),
            anvilInvOpenRecoveryEmail = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.ANVIL, Messages.get("email-recovery-anvil-email-title")),
            anvilInvOpenRecoveryCode = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.ANVIL, Messages.get("email-recovery-anvil-code-title"));

    private static final LoginType defaultLoginType = LoginType.ANVIL;

    public LoginType getLoginType() {
        switch (this) {
            case REGISTER:
                return defaultLoginType;
            //case REGISTER_PIN:
            case CHANGE_PIN:
                return LoginType.PIN;
            case LOGIN:
            case CHANGE_PASSWORD:
            case RECOVERY_EMAIL:
            case RECOVERY_CODE:
                return LoginType.ANVIL;
            default:
                throw new AlixError("Da fuq");
        }
    }

    //Live, per-keystroke feedback (see AbstractAnvilBuilder#updateText) - checkBreach=false, since a real
    //HIBP network call on every keystroke would be disastrous; the actual commit re-validates fully anyway.
    public String getInvalidityReason(String input) {
        return AlixCommonUtils.getPasswordInvalidityReasonSync(input, this.getLoginType());
    }

    public boolean indicateInvalid() {
        switch (this) {
            case REGISTER, CHANGE_PASSWORD, CHANGE_PIN:
                return true;
            case LOGIN, RECOVERY_EMAIL, RECOVERY_CODE:
                return false;
            default:
                throw new AlixError("Da fuq");
        }
    }

    public boolean isUserVerified() {
        switch (this) {
            case CHANGE_PIN:
            case CHANGE_PASSWORD:
            case RECOVERY_EMAIL:
            case RECOVERY_CODE:
                return true;
            default:
                return false;
        }
    }

    public PacketSnapshot getInvOpen() {
        switch (this) {
            case REGISTER:
                //case REGISTER_PIN:
                return anvilInvOpenRegister;
            case LOGIN:
                return anvilInvOpenLogin;
            case CHANGE_PASSWORD:
                return anvilInvOpenPasswordChange;
            case CHANGE_PIN:
                return anvilInvOpenPinChange;
            case RECOVERY_EMAIL:
                return anvilInvOpenRecoveryEmail;
            case RECOVERY_CODE:
                return anvilInvOpenRecoveryCode;
            default:
                throw new AlixError("Da fuq");
        }
    }
}