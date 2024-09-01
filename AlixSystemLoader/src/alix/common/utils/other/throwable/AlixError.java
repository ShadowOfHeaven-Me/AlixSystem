package alix.common.utils.other.throwable;

public final class AlixError extends InternalError {

    public AlixError() {
    }

    public AlixError(String message) {
        super(message);
    }

    public AlixError(Throwable e) {
        super(e);
    }

    public AlixError(String message, Throwable e) {
        super(message, e);
    }

    public AlixError(Throwable e, String message) {
        super(message, e);
    }

    public static void throwInvalid(Object whatWasInvalid) {
        throw new AlixError("Invalid: " + whatWasInvalid);
    }

    public static void init() {
    }
}