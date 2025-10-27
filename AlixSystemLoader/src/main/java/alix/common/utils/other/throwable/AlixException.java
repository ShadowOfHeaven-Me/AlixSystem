package alix.common.utils.other.throwable;

import alix.common.AlixCommonMain;

public final class AlixException extends RuntimeException {

    public AlixException(String message) {
        super(message);
        onException0(this);
    }

    public AlixException(Throwable e) {
        super(e);
        onException0(this);
    }

    public AlixException(String message, Throwable e) {
        super(message, e);
        onException0(this);
    }

    public AlixException(Throwable e, String message) {
        super(message, e);
        onException0(this);
    }

    public AlixException() {
        onException0(this);
    }

    private static final boolean debug = false;

    static void onException0(Throwable t) {
        if (!debug)
            return;
        AlixCommonMain.logError("DEBUG EXCEPTION INSTANTIATION=" + t.getMessage());
        t.printStackTrace();
    }

    public static void isTrue(boolean b, String message) {
        if (!b) throw new AlixException(message);
    }

    public static void init() {
    }
}