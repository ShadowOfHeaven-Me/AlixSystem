package shadow.systems.login.reminder.strategy;

import shadow.utils.users.types.UnverifiedUser;

final class ActionBarStrategyImpl extends ReminderStrategy {

    private static final long MESSAGE_RESEND_DELAY = 1500;
    private long nextSend;

    ActionBarStrategyImpl(UnverifiedUser user) {
        super(user);
    }

/*    public static ScheduledFuture<?> newImpl(UnverifiedUser user) {
        return user.getChannel().eventLoop().scheduleAtFixedRate(new ActionBarStrategyImpl(user), 500L, TICK_DELAY, TimeUnit.MILLISECONDS);
    }*/

    @Override
    void tick() {
        long now = System.currentTimeMillis();
        boolean delayPassed = now > this.nextSend;

        if (!delayPassed || user.getPacketBlocker().getFallPhase().isOngoing()) return;

        this.nextSend = now + MESSAGE_RESEND_DELAY;//update the next send
        this.user.getVerificationMessage().spoof();
    }
}