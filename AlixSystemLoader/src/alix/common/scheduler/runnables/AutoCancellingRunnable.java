/*
package alix.common.scheduler.runnables;

import com.google.common.base.Preconditions;

import java.util.concurrent.TimeUnit;

public abstract class AutoCancellingRunnable extends AlixRunnable {

    private int cancelAfter;

    public AutoCancellingRunnable(long interval, TimeUnit unit, boolean async, int cancelAfter) {
        super(interval, unit, async);
        Preconditions.checkArgument(cancelAfter > 0, "Cancel after must be greater than zero! (Got " + cancelAfter + ")");
        this.cancelAfter = cancelAfter;
    }

    public boolean checkForCancel() {
        if (cancelAfter-- == 0) {
            cancel();
            return true;
        }
        return false;
    }
}*/
