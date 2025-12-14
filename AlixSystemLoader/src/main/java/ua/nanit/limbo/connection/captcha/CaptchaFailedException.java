package ua.nanit.limbo.connection.captcha;

import ua.nanit.limbo.NanoLimbo;

final class CaptchaFailedException extends RuntimeException {

    static final CaptchaFailedException FAILED = new CaptchaFailedException();

    private CaptchaFailedException() {
        super();
    }

    @Override
    public Throwable fillInStackTrace() {
        //similarly to StacklessClosedChannelException
        if (!NanoLimbo.printCaptchaFailed)
            return this;

        return super.fillInStackTrace();
    }

    @Override
    public Throwable getCause() {
        //similarly to StacklessClosedChannelException
        if (!NanoLimbo.printCaptchaFailed)
            return null;

        return super.getCause();
    }
}