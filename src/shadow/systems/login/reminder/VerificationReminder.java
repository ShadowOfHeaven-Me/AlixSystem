package shadow.systems.login.reminder;

import shadow.systems.login.reminder.strategy.ReminderStrategy;
import shadow.systems.login.reminder.strategy.VerificationReminderStrategy;
import shadow.utils.users.types.UnverifiedUser;

import java.util.concurrent.ScheduledFuture;

public final class VerificationReminder {

    public static final VerificationReminderStrategy STRATEGY = VerificationReminderStrategy.determineStrategy();
    //public static final long MESSAGE_RESEND_DELAY = 1500;
    //private static final ByteBuf timeOutError = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase("§cTimed Out Fall [Alix]");

    public static ScheduledFuture<?> reminderFor(UnverifiedUser user) {
        return ReminderStrategy.newReminderImplFor(user);
    }

    /*private static void tick(UnverifiedUser user) {
        long now = System.currentTimeMillis();
        boolean delayPassed = now > user.nextSend;

        PacketBlocker blocker = user.getPacketBlocker();
        VirtualCountdown countdown = blocker.getCountdown();
        VirtualFallPhase fallPhase = blocker.getFallPhase();

        if (fallPhase.isOngoing()) countdown.tickNoPacket();
        else countdown.tick();

        if (!user.hasCompletedCaptcha() && fallPhase.timeoutTick()) {
            NettyUtils.closeAfterConstSend(user.getChannel(), timeOutError);
            return;
        }

        if (!delayPassed || fallPhase.isOngoing()) return;

        user.nextSend = now + MESSAGE_RESEND_DELAY;//update the next send
        user.getVerificationMessage().spoof();
        //ByteBuf buf;

        *//*if (*//**//*!user.isGUIInitialized() &&*//**//* (buf = user.getRawVerificationMessageBuffer()) != null)
            user.writeAndFlushRaw(buf);*//*
    }*/

    public static void init() {
    }

    private VerificationReminder() {
    }
}