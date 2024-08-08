package io.netty.channel.unix;

import alix.common.antibot.IPUtils;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.utils.other.annotation.AlixIntrinsified;
import alix.common.utils.other.annotation.RemotelyInvoked;

public final class AlixEpollConnection {

    //this can be done because of 1 thread invocation
    private static final byte[] ipv6Addr = new byte[16];

    //todo: Create new threads for accepting connections with high traffic

    //can be reimplemented in cpp
    //@OptimizationCandidate
    @RemotelyInvoked
    @AlixIntrinsified(method = "io.netty.channel.unix.Socket.accept(int, byte[])")
    public static int handle(int serverFd, byte[] addr) {
        //long t1 = System.nanoTime();
        int res = AlixSocketAccessBridge.nativeAccept(serverFd, addr);//fd
        if (res < 0) return Errors.ERRNO_EAGAIN_NEGATIVE;//just generally trying to avoid errors, not sure if this is a good idea
        //first - length

        //last 4  - port
        byte len = addr[0];

        //Source: NativeInetAddress.address(byte[], int, int)
        switch (len) {
            case 8:
                // 8 bytes:
                // - 4  == ipaddress
                // - 4  == port
                //AlixHandler.SERVER_CHANNEL_FUTURE.channel().eventLoop().
                //Integer.toUnsignedLong(
                long val = Integer.toUnsignedLong(((addr[1] & 0xff) << 24) + ((addr[2] & 0xff) << 16) + ((addr[3] & 0xff) << 8) + (addr[4] & 0xff));

                //long tA = System.nanoTime();
                if (FireWallManager.isBlocked(val)) {//fast ipv4 look-up
                    AlixSocketAccessBridge.nativeClose(res);
                    return Errors.ERRNO_EAGAIN_NEGATIVE;
                }
                //long tB = System.nanoTime();

                //long t3 = System.nanoTime();
                //double lookUpTime = (tB - tA) / Math.pow(10, 6);
                //double timeTotal = (t3 - t1) / Math.pow(10, 6);

                //Main.logError("TIME TOTAL: " + timeTotal + "ms LOOK-UP: " + lookUpTime +  " RESULT " + res + " SERVER FD: " + serverFd + " ADDR: " + Arrays.toString(addr));
                return res;
            case 24:
                // 24 bytes:
                // - 16  == ipaddress
                // - 4   == scopeId
                // - 4   == port
                System.arraycopy(addr, 1, ipv6Addr, 0, 16);
                if (FireWallManager.isBlocked(IPUtils.fastIpv6(ipv6Addr))) {//general ipv6 look-up with fast object creation
                    AlixSocketAccessBridge.nativeClose(res);
                    return Errors.ERRNO_EAGAIN_NEGATIVE;
                }
                return res;
            default:
                AlixSocketAccessBridge.nativeClose(res);
                return Errors.ERRNO_EAGAIN_NEGATIVE;
        }
    }

    public static void init() {
    }
}