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

    public AlixException() {
    }

    public static void isTrue(boolean b, String message) {
        if (!b) throw new AlixException(message);
    }

    public static void init() {
    }
}