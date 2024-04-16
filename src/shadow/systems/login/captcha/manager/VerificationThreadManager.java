/*
package shadow.systems.login.captcha.manager;

import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import shadow.systems.login.reminder.VerificationReminder;

public final class VerificationThreadManager {

    public static void initialize() {
        CountdownTask.pregenerate();
        //AlixScheduler.newAlixThread(new VerificationThreadRunnable(), 200, "Verification Thread"); //todo: proportional 3
        //the "todos" are for the sole purpose of code visibility
    }

    private static final class VerificationThreadRunnable implements Runnable {

        private byte repeats;

        @Override
        public void run() {
            //Verification message reminder
            //Captcha countdown and kick out
            VerificationReminder.sendVerificationMessagesAndUpdateTimer();

            if (++this.repeats == 5) {
                this.repeats = 0;
                AntiBotStatistics.INSTANCE.reset();
            }
        }

        private VerificationThreadRunnable() {
        }
    }
}*/
