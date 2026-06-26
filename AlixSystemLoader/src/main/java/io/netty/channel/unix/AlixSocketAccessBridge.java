package io.netty.channel.unix;

import alix.common.utils.other.annotation.RemotelyInvoked;
import lombok.SneakyThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public final class AlixSocketAccessBridge {

    private static final MutableCallSite ACCEPT_CALL_SITE;
    private static final MethodHandle MH, ACCEPT, CLOSE;

    static {
        try {
            MethodType acceptType = MethodType.methodType(int.class, int.class, byte[].class);
            ACCEPT_CALL_SITE = new MutableCallSite(acceptType);
            // to make sure we initialize a static final MethodHandle for Socket#accept(...)
            // so that we do not lose out on JVM HotSpot optimizations
            MH = ACCEPT_CALL_SITE.dynamicInvoker();

            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Socket.class, MethodHandles.lookup());
            ACCEPT = lookup.findStatic(Socket.class, "accept", acceptType);

            lookup = MethodHandles.privateLookupIn(FileDescriptor.class, MethodHandles.lookup());
            CLOSE = lookup.findStatic(FileDescriptor.class, "close", MethodType.methodType(int.class, int.class));
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private AlixSocketAccessBridge() {
    }

    public static void initDelegate(MethodHandle target) {
        ACCEPT_CALL_SITE.setTarget(target);
        //I don't think this actually does anything, still seems to be annotated as 'NYI',
        //without any intrinsic annotation
        //will just keep it in case they ever implement it (or some VM overrides it)
        MutableCallSite.syncAll(new MutableCallSite[]{ACCEPT_CALL_SITE});
    }

    @SneakyThrows
    @RemotelyInvoked
    public static int acceptBridge(int fd, byte[] addr) {
        return (int) MH.invokeExact(fd, addr);
    }

    @SneakyThrows
    public static int nativeAccept(int fd, byte[] addr) {
        return (int) ACCEPT.invokeExact(fd, addr);
    }

    @SneakyThrows
    public static void nativeClose(int fd) {
        int res = (int) CLOSE.invokeExact(fd);
        if (res < 0) throw new Error("Close returned " + res);
    }
}