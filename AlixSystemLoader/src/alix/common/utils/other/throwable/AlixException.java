package alix.common.utils.other.throwable;

public final class AlixException extends RuntimeException {

    public AlixException(String message) {
        super(message);
    }

    public AlixException(Throwable e) {
        super(e);
    }

    public AlixException(String message, Throwable e) {
        super(message, e);
    }

    public AlixException(Throwable e, String message) {
        super(message, e);
    }
}