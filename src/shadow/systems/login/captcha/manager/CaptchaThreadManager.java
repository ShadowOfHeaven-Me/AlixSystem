package shadow.systems.login.captcha.manager;

import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import alix.common.utils.collections.queue.ConcurrentAlixDeque;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import shadow.systems.login.Verifications;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

public final class CaptchaThreadManager {

    //public static final String captchaTimePassed = Messages.get("captcha-time-passed");
    private static final Object captchaTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(Messages.get("captcha-time-passed"));
    private static final boolean initialize = AlixUtils.requireCaptchaVerification;// && AlixUtils.maxCaptchaTime > 1;
    private static final CaptchaThreadRunnable captchaRunnable = initialize ? new CaptchaThreadRunnable() : null;
    //public static final Object CAPTCHA_LOCK = captchaRunnable;

    /*public static void regenerateCaptcha(Captcha captcha) {
        AlixScheduler.async(() -> Captcha.
        //captchaRunnable.captchasToRegen.offerLast(captcha);
    }*/

    public static void pregenerate() {
        CountdownTask.pregenerate();
        if (initialize)
            AlixScheduler.newAlixThread(captchaRunnable, 100, "Captcha Thread"); //todo: proportional 3
        //the "todos" are for the sole purpose of code visibility
    }

    private static final class CaptchaThreadRunnable implements Runnable {

        //private final AlixDeque<Captcha> captchasToRegen = new ConcurrentAlixDeque<>();
        //private final AlixDeque<Channel> kicked = new AlixDeque<>();
        //private final MergedTask mergedTask = new MergedTask();
        //private final int maxPlayers = Bukkit.getMaxPlayers();
        //private volatile boolean whilstCopying = false;
        private byte repeats;

        @Override
        public void run() {
            //Verification message reminder
            //Captcha countdown and kick out

            boolean verMsgDelayPassed = VerificationReminder.hasDelayPassed();

            //Verifications.forEach((uuid, user) -> { (?)

            for (UnverifiedUser user : Verifications.users()) {
                //if (user.isGUIInitialized()) continue;
                //TODO: Make the countdown switchable (the ability to turn it OFF)
                CountdownTask task = user.getPacketBlocker().getCountdownTask();

                if (task.tick())
                    MethodProvider.kickAsync(user, captchaTimePassedKickPacket);  // this.kicked.offerLast(user.getPacketBlocker().getChannel());//captcha countdown

                if (verMsgDelayPassed && !user.isGUIInitialized() && user.getVerificationMessagePacket() != null)
                    user.writeAndFlushSilently(user.getVerificationMessagePacket());//message reminder
            }

            if (verMsgDelayPassed) VerificationReminder.updateNextSend();

            /*if (!this.kicked.isEmpty()) {
                AlixDeque.Node<Channel> node = this.kicked.firstNode();
                //AlixScheduler.sync(() -> AlixDeque.forEach(player -> player.kickPlayer(captchaTimePassed), node));
                AlixDeque.forEach(channel -> MethodProvider.kickAsync(channel, captchaTimePassedKickPacket), node);

                this.kicked.clear();
                //this.mergedTask.setTask(() -> AlixDeque.forEach(player -> player.kickPlayer(captchaTimePassed), this.kicked.firstNode()));
            }*/

            //Captcha Regeneration Logic

            /*if (!this.captchasToRegen.isEmpty()) {
                AlixDeque.Node<Captcha> firstNode = this.captchasToRegen.firstNode();
                this.captchasToRegen.clear();

                AlixDeque.forEach(Captcha::regenerate, firstNode);
                Captcha.addToPool(firstNode);
            }*/

            if (++repeats == 5) {
                this.repeats = 0;
                AntiBotStatistics.INSTANCE.reset();
            }
        }

        private CaptchaThreadRunnable() {
        }
    }
}