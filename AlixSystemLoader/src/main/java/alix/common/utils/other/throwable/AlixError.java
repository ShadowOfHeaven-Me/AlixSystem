package alix.common.utils.other.throwable;

import static alix.common.utils.other.throwable.AlixException.onException0;

public final class AlixError extends InternalError {

    public AlixError() {
        onException0(this);
    }

    public AlixError(String message) {
        super(message);
        onException0(this);
    }

    public AlixError(Throwable e) {
        super(e);
        onException0(this);
    }

    public AlixError(String message, Throwable e) {
        super(message, e);
        onException0(this);
    }

    public AlixError(Throwable e, String message) {
        super(message, e);
        onException0(this);
    }
    
    public static void throwInvalid(Object whatWasInvalid) {
        throw new AlixError("Invalid: " + whatWasInvalid);
    }

    public static void init() {
    }
}