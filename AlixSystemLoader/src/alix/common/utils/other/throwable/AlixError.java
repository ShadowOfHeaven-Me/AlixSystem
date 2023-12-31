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
}