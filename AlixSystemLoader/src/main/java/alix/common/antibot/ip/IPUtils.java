package alix.common.antibot.ip;

import alix.common.AlixCommonMain;
import alix.common.utils.other.throwable.AlixException;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IPUtils {

    private static final InetAddrUtil IMPL = InetAddrUtil.createImpl();

    public static boolean hasUnsafeImpl() {
        return IMPL instanceof AddrUtilUnsafeImpl;
    }

    public static InetAddress fromAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            AlixCommonMain.logError("Invalid address: '" + ip + "'!");
            throw new AlixException(e);
        }
    }

    public static int ipv4Value(byte[] src) {
        return ((src[0] & 0xFF) << 24) | ((src[1] & 0xFF) << 16) | ((src[2] & 0xFF) << 8) | (src[3] & 0xFF);
    }

    public static byte[] ipv4ByteArray(int src) {
        return new byte[]{(byte) (src >> 24), (byte) (src << 8 >> 24), (byte) (src << 16 >> 24), (byte) (src << 24 >> 24)};
    }

    public static void override(Inet4Address addr, int value) {
        if (!IMPL.override(addr, value))
            throw new AlixException("Cannot override due to not Unsafe impl!");
    }

    public static Inet6Address fastIPv6(byte[] addr) {
        try {
            return IMPL.fastIPv6(addr);
        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    public static int ipv4Value(Inet4Address address) {
        return IMPL.ipv4Value(address);
    }
}