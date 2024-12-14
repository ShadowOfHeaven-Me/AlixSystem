package shadow.utils.objects.savable.data;

import alix.common.data.LoginType;
import alix.common.data.security.password.Password;
import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import org.bukkit.entity.Player;
import shadow.utils.main.AlixUtils;

public final class DataChanges {

    private static final String
            mainPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-main-pin"),
            mainNotPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-main-not-pin"),
            secondaryPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-secondary-pin"),
            secondaryNotPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-secondary-not-pin"),
            secondaryNotSet = Messages.getWithPrefix("gui-password-try-apply-data-fail-secondary-password-not-set");
    private final PersistentUserData data;
    private String newPassword, newExtraPassword;
    private LoginType loginType, extraLoginType;

    public DataChanges(PersistentUserData data) {
        this.data = data;
        this.setLoginType(data.getLoginType());
        this.setExtraLoginType(data.getLoginParams().getExtraLoginType());
    }

    public boolean tryApply(Player player) {
        boolean changeMainPassword = false, changeExtraPassword = false;

        LoginType originalLoginType = data.getLoginType();

        if (newPassword == null) {//didn't change the password, but tried to change from a proper password to a pin or vice versa
            if (originalLoginType == LoginType.PIN && loginType != LoginType.PIN) {
                player.sendRawMessage(mainPin);
                return false;
            }

            if (originalLoginType != LoginType.PIN && loginType == LoginType.PIN) {
                player.sendRawMessage(mainNotPin);
                return false;
            }

            LoginType extraLoginType = data.getLoginParams().getExtraLoginType();

            if (this.extraLoginType != null && newExtraPassword == null) {
                if (extraLoginType == LoginType.PIN && this.extraLoginType != LoginType.PIN) {
                    player.sendRawMessage(secondaryPin);
                    return false;
                }

                if (extraLoginType != LoginType.PIN && this.extraLoginType == LoginType.PIN) {
                    player.sendRawMessage(secondaryNotPin);
                    return false;
                }
            }
        }

        if (newPassword != null) {
            String reason = AlixUtils.getPasswordInvalidityReason(newPassword, loginType);
            if (reason == null) changeMainPassword = true;
            else {
                player.sendRawMessage(reason);
                return false;
            }
        }

        if (extraLoginType != null) {
            if (newExtraPassword == null && data.getLoginParams().getExtraPassword() == null) {//
                player.sendRawMessage(secondaryNotSet);
                return false;
            }
            if (newExtraPassword != null) {
                String reason = AlixUtils.getPasswordInvalidityReason(newExtraPassword, extraLoginType);
                if (reason == null) changeExtraPassword = true;
                else {
                    player.sendRawMessage(reason);
                    return false;
                }
            }
            LoginType extraLoginType = data.getLoginParams().getExtraLoginType();

            if (this.extraLoginType != null && newExtraPassword == null) {
                if (extraLoginType == LoginType.PIN && this.extraLoginType != LoginType.PIN) {
                    player.sendRawMessage(secondaryPin);
                    return false;
                }

                if (extraLoginType != LoginType.PIN && this.extraLoginType == LoginType.PIN) {
                    player.sendRawMessage(secondaryNotPin);
                    return false;
                }
            }
        }
        if (changeMainPassword) data.setPassword(newPassword);
        if (changeExtraPassword) data.getLoginParams().setExtraPassword(Password.fromUnhashed(newExtraPassword));

        data.setLoginType(loginType);
        data.getLoginParams().setExtraLoginType(extraLoginType);
        return true;
    }

    public String getPassword() {
        return newPassword;
    }

    public void setPassword(String password) {
        this.newPassword = password;
    }

    public String getExtraPassword() {
        return newExtraPassword;
    }

    public void setExtraPassword(String extraPassword) {
        this.newExtraPassword = extraPassword;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public void setLoginType(LoginType loginType) {
        this.loginType = loginType;
    }

    public LoginType getExtraLoginType() {
        return extraLoginType;
    }

    public void setExtraLoginType(LoginType extraLoginType) {
        this.extraLoginType = extraLoginType;
    }
}