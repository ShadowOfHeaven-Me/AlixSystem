package shadow.systems.login.reminder.message;

import shadow.utils.users.types.UnverifiedUser;

public interface VerificationMessage {

    void updateAfterCaptchaComplete();

    void updateMessage();

    void spoof();

    void clearEffects();

    void destroy();

    static VerificationMessage createFor(UnverifiedUser user) {
        return AbstractVerificationMessage.createFor0(user);
    }
}