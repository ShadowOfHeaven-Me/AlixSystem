package shadow.systems.login.captcha.types;

import alix.common.utils.AlixCommonUtils;
import shadow.Main;
import shadow.utils.main.AlixUtils;

public enum CaptchaVisualType {

    RECAPTCHA, SMOOTH, PARTICLE, MAP, SUBTITLE, MESSAGE;

    public static CaptchaVisualType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            return SMOOTH;
        }
    }

    public static CaptchaVisualType fallback() {
        return AlixCommonUtils.isGraphicEnvironmentHeadless ? SUBTITLE : SMOOTH;
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

    public static boolean isRecaptcha() {
        return AlixUtils.captchaVerificationVisualType == RECAPTCHA;
    }

    public static boolean hasPositionLock() {
        return AlixUtils.captchaVerificationVisualType == SMOOTH;
    }
}