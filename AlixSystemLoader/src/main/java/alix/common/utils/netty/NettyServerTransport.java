package alix.common.utils.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.lang.reflect.Constructor;
import java.util.concurrent.ThreadFactory;

/**
 * Picks the fastest Netty server-socket transport actually available at runtime: io_uring on Linux (only
 * if the separate, optional "netty-incubator-transport-io_uring" artifact happens to be on the classpath -
 * this plugin never depends on it directly, so it's detected purely via reflection), then Epoll on Linux
 * (bundled with the Netty distribution Velocity itself already ships, so this one is always available
 * there), falling back to plain NIO everywhere else (Windows, macOS, or a Linux setup where neither native
 * transport could be loaded).
 */
public final class NettyServerTransport {

    public static final NettyServerTransport INSTANCE = detect();

    public final String name;
    public final Class<? extends ServerChannel> serverChannelClass;
    private final EventLoopGroupFactory eventLoopGroupFactory;

    private NettyServerTransport(String name, Class<? extends ServerChannel> serverChannelClass, EventLoopGroupFactory eventLoopGroupFactory) {
        this.name = name;
        this.serverChannelClass = serverChannelClass;
        this.eventLoopGroupFactory = eventLoopGroupFactory;
    }

    public EventLoopGroup newEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        return eventLoopGroupFactory.create(nThreads, threadFactory);
    }

    @FunctionalInterface
    private interface EventLoopGroupFactory {
        EventLoopGroup create(int nThreads, ThreadFactory threadFactory);
    }

    @SuppressWarnings("unchecked")
    private static NettyServerTransport detect() {
        try {
            NettyServerTransport ioUring = tryIoUring();
            if (ioUring != null) return ioUring;
        } catch (Throwable ignored) {
            //incubator jar not present (by far the common case), or any other reflection hiccup - fall
            //through to Epoll/NIO below rather than ever failing server startup over an optional transport
        }

        if (Epoll.isAvailable())
            return new NettyServerTransport("epoll", EpollServerSocketChannel.class, EpollEventLoopGroup::new);

        return new NettyServerTransport("NIO", NioServerSocketChannel.class, NioEventLoopGroup::new);
    }

    @SuppressWarnings("unchecked")
    private static NettyServerTransport tryIoUring() throws ReflectiveOperationException {
        Class<?> ioUringClass = Class.forName("io.netty.incubator.channel.uring.IoUring");
        if (!(boolean) ioUringClass.getMethod("isAvailable").invoke(null)) return null;

        Class<? extends ServerChannel> channelClass = (Class<? extends ServerChannel>)
                Class.forName("io.netty.incubator.channel.uring.IoUringServerSocketChannel");
        Constructor<?> groupConstructor = Class.forName("io.netty.incubator.channel.uring.IoUringEventLoopGroup")
                .getConstructor(int.class, ThreadFactory.class);

        return new NettyServerTransport("io_uring", channelClass, (nThreads, threadFactory) -> {
            try {
                return (EventLoopGroup) groupConstructor.newInstance(nThreads, threadFactory);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to construct an IoUringEventLoopGroup via reflection", e);
            }
        });
    }
}
