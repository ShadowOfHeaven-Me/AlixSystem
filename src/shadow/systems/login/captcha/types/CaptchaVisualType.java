package shadow.systems.login.captcha.types;

import shadow.Main;
import shadow.utils.main.AlixUtils;

public enum CaptchaVisualType {

    RECAPTCHA, SMOOTH, PARTICLE, MAP, SUBTITLE, MESSAGE;

    public static CaptchaVisualType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            return SUBTITLE;
        }
    }

    public static boolean shouldSendBlindness() {
        boolean config = Main.config.getBoolean("verification-blindness");
        if (!config) return false;

        switch (AlixUtils.captchaVerificationVisualType) {
            case RECAPTCHA:
            case SMOOTH:
            case PARTICLE:
                return false;
            default:
                return true;
        }
    }
}