package shadow.systems.login.reminder.message;

import alix.common.utils.other.throwable.AlixError;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.utils.users.types.UnverifiedUser;

abstract class AbstractVerificationMessage implements VerificationMessage {

    final UnverifiedUser user;

    AbstractVerificationMessage(UnverifiedUser user) {
        this.user = user;
    }

    static VerificationMessage createFor0(UnverifiedUser user) {
        switch (VerificationReminder.STRATEGY) {
            case TITLE:
                return new TitleVerificationMessage(user);
            case ACTION_BAR:
                return new ActionBarVerificationMessage(user);
            default:
                throw new AlixError("Invalid: " + VerificationReminder.STRATEGY + "!");
        }
    }
}