package shadow.systems.login.captcha.types;

public enum CaptchaVisualType {

    MAP, SUBTITLE, MESSAGE;

    public static CaptchaVisualType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            return SUBTITLE;
        }
    }
}