package shadow.systems.login.captcha.manager;

import alix.common.messages.Messages;
import alix.common.scheduler.impl.AlixScheduler;
import alix.common.scheduler.tasks.MergedTask;
import alix.common.utils.collections.queue.AlixDeque;
import org.bukkit.entity.Player;
import shadow.systems.login.Verifications;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

public class CaptchaThreadManager {

    private static final String captchaTimePassed = Messages.get("captcha-time-passed");
    private static final boolean initialize = AlixUtils.requireCaptchaVerification;// && AlixUtils.maxCaptchaTime > 1;
    private static final CaptchaThreadRunnable taskManager;

    static {
        taskManager = initialize ? new CaptchaThreadRunnable() : null;
    }

    public static void regenerateCaptcha(Captcha captcha) {
        taskManager.captchasToRegen.offerLast(captcha);
    }

    public static void pregenerate() {
        if (!initialize) return;
        CountdownTask.pregenerate();
        AlixScheduler.newAlixThread(taskManager, 100, "Captcha Thread"); //todo: proportional 3
        //the "todos" are for the sole purpose of code visibility
    }

    private static final class CaptchaThreadRunnable implements Runnable {

        private final AlixDeque<Captcha> captchasToRegen = new AlixDeque<>();
        private final AlixDeque<Player> kicked = new AlixDeque<>();
        private final MergedTask mergedTask = new MergedTask();
        //private final int maxPlayers = Bukkit.getMaxPlayers();
        //private volatile boolean whilstCopying = false;

        @Override
        public void run() {
            //Verification message reminder
            //Captcha countdown and kick out

            boolean verMsgDelayPassed = VerificationReminder.hasDelayPassed();

            //Verifications.forEach((uuid, user) -> { (?)

            for (UnverifiedUser user : Verifications.users()) {
                if (user.isGUIInitialized()) continue;

                CountdownTask task = user.getPacketBlocker().getCountdownTask();

                if (task != null && task.tick()) this.kicked.offerLast(user.getPlayer());//captcha countdown

                if (verMsgDelayPassed && user.getVerificationMessage() != null) {
                    user.getPlayer().sendRawMessage(user.getVerificationMessage());//message reminder
                }
            }

            if (verMsgDelayPassed) VerificationReminder.updateNextSend();

            if (!this.kicked.isEmpty()) {
                this.mergedTask.setTask(() -> AlixDeque.forEach(player -> player.kickPlayer(captchaTimePassed), this.kicked.firstNode()));

                this.kicked.clear();
            }

            //Captcha Regeneration Logic

            if (!this.captchasToRegen.isEmpty()) {

                AlixDeque.Node<Captcha> firstNode = this.captchasToRegen.firstNode();
                this.captchasToRegen.clear();

                AlixDeque.forEach(Captcha::regenerate, firstNode);
                this.mergedTask.mergeOrSet(() -> Captcha.addToPool(firstNode));
            }

            this.mergedTask.executeSyncAndRemove();
        }
    }
}