package alix.common.antibot.epoll;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.ip.IPUtils;
import alix.common.utils.other.annotation.AlixIntrinsified;
import alix.common.utils.other.annotation.RemotelyInvoked;
import io.netty.channel.unix.AlixSocketAccessBridge;
import io.netty.channel.unix.Errors;

import java.net.Inet6Address;

public final class AlixEpollConnection {

    private static final ThreadLocal<Ipv6Cache> ipv6Cache = ThreadLocal.withInitial(Ipv6Cache::new);

    //todo: Binds threads like re-use-port, for accepting connections with high traffic

    @RemotelyInvoked
    @AlixIntrinsified(method = "io.netty.channel.unix.Socket.accept(int, byte[])")
    public static int handle(int serverFd, byte[] addr) {
        //long t1 = System.nanoTime();
        int res = AlixSocketAccessBridge.nativeAccept(serverFd, addr);//fd
        if (res < 0)
            return res;// return Errors.ERRNO_EAGAIN_NEGATIVE;//just generally trying to avoid errors, not sure if this is a good idea

        if (Telemetry.ENABLED)
            TelemetryProfiler.PROFILER.onConnection(res, addr);
        //first - length

        //last 4  - port
        byte len = addr[0];

        //Source: NativeInetAddress.address(byte[], int, int)
        switch (len) {
            case 8:
                // 8 bytes:
                // - 4  == ipaddress
                // - 4  == port
                int val = ((addr[1] & 0xff) << 24) | ((addr[2] & 0xff) << 16) | ((addr[3] & 0xff) << 8) | (addr[4] & 0xff);

                //long tA = System.nanoTime();
                if (FireWallManager.isV4Blocked0(val)) {//fast ipv4 look-up
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
                var cache = ipv6Cache.get();
                System.arraycopy(addr, 1, cache.byteAddr, 0, 16);
                // hashCode() is never cached by Inet6Address
                // and equals(...) relies only on the byte[] addr
                // so we may cache if it matches byte[] addr reference

                var inet6Cache = cache.inet6Cache;
                var compareInstance = inet6Cache != null ? inet6Cache : IPUtils.fastIPv6(cache.byteAddr);//obj creation in case unavailable

                if (FireWallManager.isBlocked0(compareInstance)) {//general ipv6 look-up
                    AlixSocketAccessBridge.nativeClose(res);
                    return Errors.ERRNO_EAGAIN_NEGATIVE;
                }
                return res;
            default:
                AlixSocketAccessBridge.nativeClose(res);
                return Errors.ERRNO_EAGAIN_NEGATIVE;
        }
    }

    static final class Ipv6Cache {
        final byte[] byteAddr;
        final Inet6Address inet6Cache;

        Ipv6Cache() {
            this.byteAddr = new byte[16];
            this.inet6Cache = IPUtils.hasUnsafeImpl() ? IPUtils.fastIPv6(this.byteAddr) : null;
        }
    }

    public static void init() {
    }
}