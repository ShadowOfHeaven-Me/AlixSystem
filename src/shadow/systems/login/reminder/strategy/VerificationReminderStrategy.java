package shadow.systems.login.reminder.strategy;

import shadow.Main;

public enum VerificationReminderStrategy {

    TITLE,
    ACTION_BAR;

    public static VerificationReminderStrategy determineStrategy() {
        String s = Main.config.getString("verification-reminder-type");

        try {
            return VerificationReminderStrategy.valueOf(s.toUpperCase());
        } catch (Exception e) {
            Main.logWarning("Invalid verification-reminder-type, set to '" + s + "', with available being: title & action_bar!" +
                    "'title' will be used, as default.");
            return TITLE;
        }
    }
}