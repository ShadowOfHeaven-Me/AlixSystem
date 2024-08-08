package alix.common.antibot.firewall;

import alix.common.utils.other.throwable.AlixException;

public enum FireWallType {

    NETTY,
    INTERNAL_NIO_INTERCEPTOR,
    FAST_UNSAFE_EPOLL,
    OS_IPSET;

    public static final FireWallType[] USED = new FireWallType[1];

    public static boolean isIPv4FastLookUpEnabled() {
        if (USED[0] == null) throw new AlixException("FireWall Type not defined!");
        return USED[0] == FAST_UNSAFE_EPOLL;
    }
}