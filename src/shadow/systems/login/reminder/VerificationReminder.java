package shadow.systems.login.reminder;

import shadow.systems.login.Verifications;
import shadow.utils.users.offline.UnverifiedUser;

public final class VerificationReminder {

    public static final long MILLIS_UPDATE_PERIOD = 1500;
    private static long nextSend = System.currentTimeMillis();

    public static void sendVerificationMessagesAndUpdateTimer() {
        long now = System.currentTimeMillis();

        boolean delayPassed = now > nextSend;

        if (delayPassed) nextSend = now + MILLIS_UPDATE_PERIOD;//update the next send

        //Main.logInfo("REPEAT");

        for (UnverifiedUser user : Verifications.users()) {

            user.getPacketBlocker().getCountdownTask().tick();

            //Main.logInfo("USER " + delayPassed + " " + !user.isGUIInitialized() + " " + (user.getVerificationMessagePacket() != null));

            if (delayPassed && !user.isGUIInitialized() && user.getVerificationMessagePacket() != null)
                //Main.logInfo("REMINDERR");
                user.writeAndFlushSilently(user.getVerificationMessagePacket());
        }
    }

    public static void init() {
    }

    private VerificationReminder() {
    }
}