package shadow.systems.login.reminder.strategy;

import shadow.utils.users.types.UnverifiedUser;

final class TitleStrategyImpl extends ReminderStrategy {

    //private static final long TICK_DELAY = 1000 / BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND;

    TitleStrategyImpl(UnverifiedUser user) {
        super(user);
    }

/*    public static ScheduledFuture<?> newImpl(UnverifiedUser user) {
        return user.getChannel().eventLoop().scheduleAtFixedRate(new TitleStrategyImpl(user), 500L, TICK_DELAY, TimeUnit.MILLISECONDS);
    }*/

    @Override
    void tick() {
        if (!this.user.hasCompletedCaptcha()) this.user.getVerificationMessage().spoof();
    }
}