package io.netty.channel.unix;

import alix.common.AlixCommonMain;
import alix.common.utils.other.annotation.RemotelyInvoked;
import alix.common.utils.other.throwable.AlixError;
import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class AlixSocketAccessBridge {

    private static final MethodHandle MH, ACCEPT, CLOSE;

    static {
        try {
            var plugin = AlixCommonMain.MAIN_CLASS_INSTANCE; //Bukkit.getPluginManager().getPlugin("AlixSystem");
            //ClassLoader cl = (ClassLoader) plugin.getClass().getMethod("getJarInJarLoader").invoke(plugin);
            ClassLoader cl = plugin.getClass().getClassLoader();

            MH = MethodHandles.lookup().findStatic(Class.forName("io.netty.channel.unix.AlixEpollConnection", true, cl), "handle",
                    MethodType.methodType(int.class, int.class, byte[].class));
            //MH = null;
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Socket.class, MethodHandles.lookup());
            ACCEPT = lookup.findStatic(Socket.class, "accept", MethodType.methodType(int.class, int.class, byte[].class));
            lookup = MethodHandles.privateLookupIn(FileDescriptor.class, MethodHandles.lookup());
            CLOSE = lookup.findStatic(FileDescriptor.class, "close", MethodType.methodType(int.class, int.class));
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private AlixSocketAccessBridge() {
    }

    @SneakyThrows
    @RemotelyInvoked
    static int acceptBridge(int fd, byte[] addr) {
        return (int) MH.invokeExact(fd, addr);
    }

    @SneakyThrows
    static int nativeAccept(int fd, byte[] addr) {
        return (int) ACCEPT.invokeExact(fd, addr);
    }

    @SneakyThrows
    static void nativeClose(int fd) {
        int res = (int) CLOSE.invokeExact(fd);
        if (res < 0) throw new AlixError("Close returned " + res);
    }
}