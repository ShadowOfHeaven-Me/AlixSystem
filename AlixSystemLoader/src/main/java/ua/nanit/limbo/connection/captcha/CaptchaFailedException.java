package ua.nanit.limbo.connection.captcha;

final class CaptchaFailedException extends RuntimeException {

    static final CaptchaFailedException FAILED = new CaptchaFailedException();

    private CaptchaFailedException() {
        super();
    }
}