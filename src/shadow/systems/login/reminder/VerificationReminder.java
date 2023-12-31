package shadow.systems.login.reminder;

import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.queue.AlixDeque;
import io.netty.channel.Channel;
import shadow.systems.commands.CommandManager;
import shadow.systems.login.Verifications;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.TimeUnit;

public final class VerificationReminder {

    private static long nextSend = System.currentTimeMillis();
    private static final AlixDeque<Channel> kicked = !AlixUtils.requireCaptchaVerification ? new AlixDeque<>() : null;
    private static final Object loginTimePassedKickPacket = !AlixUtils.requireCaptchaVerification ? OutDisconnectKickPacketConstructor.constructAtPlayPhase(Messages.get("login-time-passed")) : null;

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

    //synchronized in order to ensure that this method is never executed
    private static synchronized void sendVerificationMessagesAndUpdateTimer() {
        boolean delayPassed = hasDelayPassed();

        if (delayPassed) updateNextSend();

        for (UnverifiedUser user : Verifications.users()) {
            if (delayPassed && user.getVerificationMessage() != null)
                user.getPlayer().sendRawMessage(user.getVerificationMessage());
            if (user.getPacketBlocker().getCountdownTask().tick()) kicked.offerLast(user.getPacketBlocker().getChannel());
        }
        AlixDeque.Node<Channel> node = kicked.firstNode();
        kicked.clear();

        if (node != null) AlixDeque.forEach(channel -> MethodProvider.kickAsync(channel, loginTimePassedKickPacket), node);

        //AlixScheduler.sync(() -> AlixDeque.forEach(player -> player.kickPlayer(CommandManager.), node));
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