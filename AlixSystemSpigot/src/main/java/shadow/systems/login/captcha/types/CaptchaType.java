package shadow.systems.login.captcha.types;

public enum CaptchaType {

    NUMERIC, TEXT;

    public static CaptchaType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            return TEXT;
        }
    }
}