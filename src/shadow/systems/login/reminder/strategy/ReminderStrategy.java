package shadow.systems.login.reminder.strategy;

import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.manager.VirtualCountdown;
import shadow.systems.login.captcha.types.CaptchaVisualType;
import shadow.systems.login.reminder.VerificationReminder;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.objects.packet.check.fall.VirtualFallPhase;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.users.types.UnverifiedUser;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ReminderStrategy implements Runnable {

    private static final long TICK_DELAY = 1000 / BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND;
    private static final ByteBuf timeOutError = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase("§cTimed Out Fall [Alix]");
    final UnverifiedUser user;

    ReminderStrategy(UnverifiedUser user) {
        this.user = user;
    }

    public static ScheduledFuture<?> newReminderImplFor(UnverifiedUser user) {
        return user.getChannel().eventLoop().scheduleAtFixedRate(strategy(user), 500L, TICK_DELAY, TimeUnit.MILLISECONDS);
    }

    private static ReminderStrategy strategy(UnverifiedUser user) {
        switch (VerificationReminder.STRATEGY) {
            case ACTION_BAR:
                return new ActionBarStrategyImpl(user);
            case TITLE:
                return new TitleStrategyImpl(user);
            default:
                throw new AlixError("Invalid: " + VerificationReminder.STRATEGY);
        }
    }

    abstract void tick();

    @Override
    public final void run() {
        PacketBlocker blocker = this.user.getPacketBlocker();
        VirtualCountdown countdown = blocker.getCountdown();
        VirtualFallPhase fallPhase = blocker.getFallPhase();

        if (fallPhase.isOngoing() || this.user.captchaInitialized() && CaptchaVisualType.hasPositionLock()) countdown.tickNoPacket();
        else countdown.tick();

        if (!this.user.hasCompletedCaptcha() && fallPhase.timeoutTick()) {
            NettyUtils.closeAfterConstSend(this.user.silentContext(), timeOutError);
            return;
        }
        this.tick();
    }
}