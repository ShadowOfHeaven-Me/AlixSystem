package alix.common.antibot.ip;

import alix.common.utils.other.AlixUnsafe;

import java.net.Inet4Address;
import java.net.Inet6Address;

interface InetAddrUtil {

    Inet6Address fastIPv6(byte[] addr) throws Exception;

    int ipv4Value(Inet4Address address);

    static InetAddrUtil createImpl() {
        return AlixUnsafe.hasUnsafe() ? new AddrUtilUnsafeImpl() : new AddrUtilAPIImpl();
    }
}