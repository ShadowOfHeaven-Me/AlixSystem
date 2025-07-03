package alix.common.antibot.ip;

import alix.common.AlixCommonMain;
import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.throwable.AlixException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IPUtils {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    private static final long holderOffset, addressOffset;//ipv4
    private static final long holder6Offset, ipaddress6Offset;//ipv6
    private static final Class<?> holder6Clazz;

    static {
        try {
            Field holderField = InetAddress.class.getDeclaredField("holder");
            holderOffset = UNSAFE.objectFieldOffset(holderField);
            addressOffset = UNSAFE.objectFieldOffset(holderField.getType().getDeclaredField("address"));

            Field holder6Field = Inet6Address.class.getDeclaredField("holder6");
            holder6Offset = UNSAFE.objectFieldOffset(holder6Field);
            holder6Clazz = holder6Field.getType();
            ipaddress6Offset = UNSAFE.objectFieldOffset(holder6Clazz.getDeclaredField("ipaddress"));
        } catch (NoSuchFieldException e) {
            throw new AlixException(e);
        }
    }

    public static InetAddress fromAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            AlixCommonMain.logError("Invalid address: '" + ip + "'!");
            throw new AlixException(e);
        }
    }

    public static Inet6Address fastIPv6(byte[] addr) {
        try {
            Object holder6 = UNSAFE.allocateInstance(holder6Clazz);
            UNSAFE.putObject(holder6, ipaddress6Offset, addr);
            Inet6Address ipv6 = (Inet6Address) UNSAFE.allocateInstance(Inet6Address.class);
            UNSAFE.putObject(ipv6, holder6Offset, holder6);
            return ipv6;
        } catch (InstantiationException e) {
            throw new AlixException(e);
        }
    }

    //kinda safer than using Inet4Address::hashCode
    public static int ipv4Value(Inet4Address address) {
        Object holder = UNSAFE.getObject(address, holderOffset);
        return UNSAFE.getInt(holder, addressOffset);
    }
}