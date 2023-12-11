package shadow.systems.login.reminder;

import alix.common.scheduler.impl.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import org.bukkit.entity.Player;
import shadow.systems.login.Verifications;
import shadow.systems.login.captcha.manager.CaptchaThreadManager;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.TimeUnit;

public final class VerificationReminder {

    private static long nextSend = System.currentTimeMillis();
    private static final AlixDeque<Player> kicked = AlixUtils.requireCaptchaVerification ? null : new AlixDeque<>();

    public static void init() {
        if (!AlixUtils.requireCaptchaVerification)
            AlixScheduler.repeatAsync(VerificationReminder::sendVerificationMessagesAndUpdateTimer, 100, TimeUnit.MILLISECONDS);

    }

    public static boolean hasDelayPassed() {
        return System.currentTimeMillis() > nextSend;
    }

    public static void updateNextSend() {
        nextSend = System.currentTimeMillis() + AlixUtils.verificationReminderDelay;
    }

/*    public static void trySendVerificationMessages() {
        long now = System.currentTimeMillis();
        if (now > nextSend) {
            sendVerificationMessages();
            nextSend += AlixUtils.verificationReminderDelay; //= now + AlixUtils.verificationReminderDelay;
        }
    }*/

    //synchronized in order to ensure that the execution of this method does not take longer than the repeat interval
    private static synchronized void sendVerificationMessagesAndUpdateTimer() {
        boolean delayPassed = hasDelayPassed();

        if (delayPassed) updateNextSend();

        for (UnverifiedUser user : Verifications.users()) {
            if (delayPassed && user.getVerificationMessage() != null)
                user.getPlayer().sendRawMessage(user.getVerificationMessage());
            if (user.getPacketBlocker().getCountdownTask().tick()) kicked.offerLast(user.getPlayer());
        }
        AlixDeque.Node<Player> node = kicked.firstNode();
        kicked.clear();

        if (node == null) return;
        AlixScheduler.sync(() -> AlixDeque.forEach(player -> player.kickPlayer(CaptchaThreadManager.captchaTimePassed), node));
    }

    private VerificationReminder() {
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