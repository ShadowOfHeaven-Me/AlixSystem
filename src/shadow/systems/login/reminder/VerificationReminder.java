package shadow.systems.login.reminder;

import io.netty.buffer.ByteBuf;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class VerificationReminder {

    private static final long TICK_DELAY = 1000 / BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND;
    public static final long MESSAGE_RESEND_DELAY = 1500;
    private static final ByteBuf timeOutError = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase("§cTimed Out Fall [Alix]");

    public static ScheduledFuture<?> reminderFor(UnverifiedUser user) {
        return user.getChannel().eventLoop().scheduleAtFixedRate(() -> tick(user), 2000L, TICK_DELAY, TimeUnit.MILLISECONDS);
    }

    private static void tick(UnverifiedUser user) {
        long now = System.currentTimeMillis();
        boolean delayPassed = now > user.nextSend;

        user.getPacketBlocker().getCountdown().tick();

        if (!user.hasCompletedCaptcha() && user.getPacketBlocker().getFallPhase().timeoutTick()) {
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