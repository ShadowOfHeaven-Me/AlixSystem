package alix.common.utils.netty.safety;

public final class NettySafetyException extends RuntimeException {

    NettySafetyException(String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {//traceless
        return this;
    }

    @Override
    public Throwable getCause() {
        return null;
    }

    static NettySafetyException of(String reason) {
        return new NettySafetyException(reason);
    }
}