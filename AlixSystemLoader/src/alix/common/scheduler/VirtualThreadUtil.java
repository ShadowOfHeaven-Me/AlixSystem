package alix.common.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public final class VirtualThreadUtil {

    @SuppressWarnings("JavaReflectionMemberAccess")
    @NotNull
    public static ThreadFactory virtualThreadFactory(@NotNull ThreadFactory ifAbsent) {
        try {
            Object o = Thread.class.getMethod("ofVirtual").invoke(null);
            return (ThreadFactory) o.getClass().getMethod("factory").invoke(o);
        } catch (Exception e) {
            return ifAbsent;
            //throw new AlixException(e);
        }
    }
}