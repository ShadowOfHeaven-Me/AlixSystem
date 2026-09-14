package alix.common.utils.collections.fastutil;

import alix.common.antibot.ip.IPUtils;

import java.io.UncheckedIOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class InetAddressMap<T> {

    private final Map<Integer, T> ipv4;
    private final Map<Inet6Address, T> ipv6;

    public InetAddressMap() {
        this.ipv4 = new ConcurrentHashMap<>();
        this.ipv6 = new ConcurrentHashMap<>();
    }

    public InetAddressMap(int v4Size, int v6Size) {
        this.ipv4 = new ConcurrentHashMap<>(v4Size);
        this.ipv6 = new ConcurrentHashMap<>(v6Size);
    }

    public T put(InetAddress ip, T value) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.put(addr(v4), value)
                : this.ipv6.put((Inet6Address) ip, value);
    }

    public T get(InetAddress ip) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.get(addr(v4))
                : this.ipv6.get(ip);
    }

    public T getOrDefault(InetAddress ip, T def) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.getOrDefault(addr(v4), def)
                : this.ipv6.getOrDefault(ip, def);
    }

    public boolean containsKey(InetAddress ip) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.containsKey(addr(v4))
                : this.ipv6.containsKey(ip);
    }

    public boolean containsV4(int val) {
        return this.ipv4.containsKey(val);
    }

    public boolean containsValue(Object value) {
        return this.ipv4.containsValue(value) || this.ipv6.containsValue(value);
    }

    public T remove(InetAddress ip) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.remove(addr(v4))
                : this.ipv6.remove(ip);
    }

    public boolean remove(InetAddress ip, Object value) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.remove(addr(v4), value)
                : this.ipv6.remove(ip, value);
    }

    public T putIfAbsent(InetAddress ip, T value) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.putIfAbsent(addr(v4), value)
                : this.ipv6.putIfAbsent((Inet6Address) ip, value);
    }

    @SuppressWarnings("UnusedReturnValue")
    public T merge(InetAddress ip, T def, BiFunction<T, T, T> remappingFunction) {
        return ip instanceof Inet4Address v4
                ? this.ipv4.merge(addr(v4), def, remappingFunction)
                : this.ipv6.merge((Inet6Address) ip, def, remappingFunction);
    }

    public T computeIfAbsent(InetAddress ip, Function<? super InetAddress, ? extends T> mappingFunction) {
        if (ip instanceof Inet4Address v4) {
            return this.ipv4.computeIfAbsent(addr(v4), k -> mappingFunction.apply(v4));
        } else if (ip instanceof Inet6Address v6) {
            return this.ipv6.computeIfAbsent(v6, mappingFunction);
        }
        return null;
    }

    public T computeIfPresent(InetAddress ip, BiFunction<? super InetAddress, ? super T, ? extends T> remappingFunction) {
        if (ip instanceof Inet4Address v4) {
            return this.ipv4.computeIfPresent(addr(v4), (k, oldVal) -> remappingFunction.apply(v4, oldVal));
        } else if (ip instanceof Inet6Address v6) {
            return this.ipv6.computeIfPresent(v6, remappingFunction);
        }
        return null;
    }

    public T compute(InetAddress ip, BiFunction<? super InetAddress, ? super T, ? extends T> remappingFunction) {
        if (ip instanceof Inet4Address v4) {
            return this.ipv4.compute(addr(v4), (k, oldVal) -> remappingFunction.apply(v4, oldVal));
        } else if (ip instanceof Inet6Address v6) {
            return this.ipv6.compute(v6, remappingFunction);
        }
        return null;
    }

    public int size() {
        return this.ipv4.size() + this.ipv6.size();
    }

    public boolean isEmpty() {
        return this.ipv4.isEmpty() && this.ipv6.isEmpty();
    }

    public void clear() {
        this.ipv4.clear();
        this.ipv6.clear();
    }

    public void forEach(BiConsumer<? super InetAddress, ? super T> action) {
        this.ipv4.forEach((k, v) -> action.accept(intToInet4Address(k), v));
        this.ipv6.forEach(action);
    }

    public Set<InetAddress> keySet() {
        Set<InetAddress> keys = new HashSet<>(size());
        this.ipv4.keySet().forEach(k -> keys.add(intToInet4Address(k)));
        keys.addAll(this.ipv6.keySet());
        return keys;
    }

    public Collection<T> values() {
        Collection<T> allValues = new ArrayList<>(size());
        allValues.addAll(this.ipv4.values());
        allValues.addAll(this.ipv6.values());
        return allValues;
    }

    int addr(Inet4Address address) {
        return IPUtils.ipv4Value(address);
    }

    public static Inet4Address intToInet4Address(int val) {
        try {
            byte[] bytes = new byte[]{
                    (byte) (val >>> 24),
                    (byte) (val >>> 16),
                    (byte) (val >>> 8),
                    (byte) val
            };
            return (Inet4Address) InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            throw new UncheckedIOException("Failed to convert integer back to Inet4Address", e);
        }
    }
}