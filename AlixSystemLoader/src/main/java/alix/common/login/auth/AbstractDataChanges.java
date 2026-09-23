package alix.common.login.auth;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.security.password.Password;
import alix.common.messages.Messages;
import alix.common.utils.AlixCommonUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractDataChanges<T> {

    private static final String
            mainPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-main-pin"),
            mainNotPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-main-not-pin"),
            secondaryPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-secondary-pin"),
            secondaryNotPin = Messages.getWithPrefix("gui-password-try-apply-data-fail-secondary-not-pin"),
            secondaryNotSet = Messages.getWithPrefix("gui-password-try-apply-data-fail-secondary-password-not-set");
    private final PersistentUserData data;
    private final BiConsumer<T, String> sendMessage;
    private String newPassword, newExtraPassword;
    private LoginType loginType, extraLoginType;

    public AbstractDataChanges(PersistentUserData data, BiConsumer<T, String> sendMessage) {
        this.data = data;
        this.sendMessage = sendMessage;
        this.setLoginType(data.getLoginType());
        this.setExtraLoginType(data.getLoginParams().getExtraLoginType());
    }

    //Callback-based (rather than returning boolean directly) because password validation can involve a real
    //HaveIBeenPwned HTTP call - see AlixCommonUtils#getPasswordInvalidityReasonAsync - which must never block
    //whatever thread calls this. The callback fires immediately, on the calling thread, whenever that HTTP
    //call isn't needed (the overwhelming majority of calls).
    public void tryApply(T player, Consumer<Boolean> callback) {
        LoginType originalLoginType = data.getLoginType();

        if (newPassword == null) {//didn't change the password, but tried to change from a proper password to a pin or vice versa
            if (originalLoginType == LoginType.PIN && loginType != LoginType.PIN) {
                this.sendMessage.accept(player, mainPin);
                callback.accept(false);
                return;
            }

            if (originalLoginType != LoginType.PIN && loginType == LoginType.PIN) {
                this.sendMessage.accept(player, mainNotPin);
                callback.accept(false);
                return;
            }

            LoginType currentExtraLoginType = data.getLoginParams().getExtraLoginType();

            if (this.extraLoginType != null && newExtraPassword == null) {
                if (currentExtraLoginType == LoginType.PIN && this.extraLoginType != LoginType.PIN) {
                    this.sendMessage.accept(player, secondaryPin);
                    callback.accept(false);
                    return;
                }

                if (currentExtraLoginType != LoginType.PIN && this.extraLoginType == LoginType.PIN) {
                    this.sendMessage.accept(player, secondaryNotPin);
                    callback.accept(false);
                    return;
                }
            }
        }

        if (newPassword != null) {
            AlixCommonUtils.getPasswordInvalidityReasonAsync(newPassword, loginType, reason -> {
                if (reason != null) {
                    this.sendMessage.accept(player, reason);
                    callback.accept(false);
                    return;
                }
                this.tryApplyExtra(player, true, callback);
            });
            return;
        }

        this.tryApplyExtra(player, false, callback);
    }

    private void tryApplyExtra(T player, boolean changeMainPassword, Consumer<Boolean> callback) {
        if (extraLoginType != null) {
            if (newExtraPassword == null && data.getLoginParams().getExtraPassword() == null) {
                this.sendMessage.accept(player, secondaryNotSet);
                callback.accept(false);
                return;
            }
            if (newExtraPassword != null) {
                AlixCommonUtils.getPasswordInvalidityReasonAsync(newExtraPassword, extraLoginType, reason -> {
                    if (reason != null) {
                        this.sendMessage.accept(player, reason);
                        callback.accept(false);
                        return;
                    }
                    this.finishApply(player, changeMainPassword, true, callback);
                });
                return;
            }
        }
        this.finishApply(player, changeMainPassword, false, callback);
    }

    private void finishApply(T player, boolean changeMainPassword, boolean changeExtraPassword, Consumer<Boolean> callback) {
        LoginType currentExtraLoginType = data.getLoginParams().getExtraLoginType();

        if (this.extraLoginType != null && newExtraPassword == null) {
            if (currentExtraLoginType == LoginType.PIN && this.extraLoginType != LoginType.PIN) {
                this.sendMessage.accept(player, secondaryPin);
                callback.accept(false);
                return;
            }

            if (currentExtraLoginType != LoginType.PIN && this.extraLoginType == LoginType.PIN) {
                this.sendMessage.accept(player, secondaryNotPin);
                callback.accept(false);
                return;
            }
        }

        if (changeMainPassword) data.setPassword(newPassword);
        if (changeExtraPassword) data.getLoginParams().setExtraPassword(Password.fromUnhashed(newExtraPassword));

        data.setLoginType(loginType);
        data.getLoginParams().setExtraLoginType(extraLoginType);
        callback.accept(true);
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