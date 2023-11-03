package shadow.systems.login.reminder;

import alix.common.scheduler.impl.AlixScheduler;
import shadow.systems.login.Verifications;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.TimeUnit;

public class VerificationReminder {

    private static long nextSend = System.currentTimeMillis();

    public static void init() {
        if (!AlixUtils.requireCaptchaVerification)
            AlixScheduler.repeatAsync(VerificationReminder::sendVerificationMessages, AlixUtils.verificationReminderDelay, TimeUnit.MILLISECONDS);
    }

    public static boolean hasDelayPassed() {
        return System.currentTimeMillis() > nextSend;
    }

    public static void updateNextSend() {
        nextSend += AlixUtils.verificationReminderDelay;
    }

/*    public static void trySendVerificationMessages() {
        long now = System.currentTimeMillis();
        if (now > nextSend) {
            sendVerificationMessages();
            nextSend += AlixUtils.verificationReminderDelay; //= now + AlixUtils.verificationReminderDelay;
        }
    }*/

    private static void sendVerificationMessages() {
        for (UnverifiedUser user : Verifications.users())
            if (user.getVerificationMessage() != null)
                user.getPlayer().sendRawMessage(user.getVerificationMessage());
    }

/*    private static final class CaptchaThreadedVerificationReminder extends VerificationReminder {

        @Override
        public void send() {

        }
    }*/

/*    private static final class SchedulerThreadedVerificationReminder extends VerificationReminder {

        @Override
        public void send() {
            sendVerificationMessages();
        }
    }*/
}