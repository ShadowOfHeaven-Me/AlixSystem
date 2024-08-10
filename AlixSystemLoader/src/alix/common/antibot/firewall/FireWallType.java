package alix.common.antibot.firewall;

import alix.common.utils.other.throwable.AlixException;

import java.util.concurrent.atomic.AtomicReference;

public enum FireWallType {

    NETTY,
    INTERNAL_NIO_INTERCEPTOR,
    FAST_UNSAFE_EPOLL,
    OS_IPSET;

    public static final AtomicReference<FireWallType> USED = new AtomicReference<>();

    public static boolean isIPv4FastLookUpEnabled() {
        //AlixCommonMain.logError("TYPE CHECK INVOKED");
        FireWallType type = USED.get();
        if (type == null) throw new AlixException("FireWall Type not defined!");
        return type == FAST_UNSAFE_EPOLL;
    }
}