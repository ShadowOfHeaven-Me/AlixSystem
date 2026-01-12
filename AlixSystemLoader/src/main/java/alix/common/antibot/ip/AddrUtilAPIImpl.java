package alix.common.antibot.ip;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

final class AddrUtilAPIImpl implements InetAddrUtil {

    @Override
    public Inet6Address fastIPv6(byte[] addr) throws Exception {
        return (Inet6Address) InetAddress.getByAddress(addr);
    }

    @Override
    public int ipv4Value(Inet4Address address) {
        byte[] src = address.getAddress();
        return IPUtils.ipv4Value(src);
    }
}