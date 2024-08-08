package shadow.systems.login.reminder;

import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.manager.VirtualCountdown;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.objects.packet.check.fall.VirtualFallPhase;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.users.types.UnverifiedUser;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class VerificationReminder {

    private static final long TICK_DELAY = 1000 / BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND;
    public static final long MESSAGE_RESEND_DELAY = 1500;
    private static final ByteBuf timeOutError = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase("§cTimed Out Fall [Alix]");

    public static ScheduledFuture<?> reminderFor(UnverifiedUser user) {
        return user.getChannel().eventLoop().scheduleAtFixedRate(() -> tick(user), 500L, TICK_DELAY, TimeUnit.MILLISECONDS);
    }

    private static void tick(UnverifiedUser user) {
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

        if (!delayPassed) return;

        user.nextSend = now + MESSAGE_RESEND_DELAY;//update the next send
        ByteBuf buf;

        if (!user.isGUIInitialized() && (buf = user.getRawVerificationMessageBuffer()) != null)
            user.writeAndFlushRaw(buf);
    }

    public static void init() {
    }

    private VerificationReminder() {
    }
}