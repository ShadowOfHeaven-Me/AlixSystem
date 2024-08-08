package shadow.systems.netty.unsafe.nio;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.nio.AbstractNioChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import shadow.utils.main.AlixHandler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Set;

public final class AlixInternalNIOInterceptor {

    private static final ServerSocketChannel delegate;
    private static final Method implCloseSelectableChannel, implConfigureBlocking;
    private static final Field chField;

    static {
        try {
            //cast the class to ensure we got the right one
            NioServerSocketChannel serverSocketChannel = (NioServerSocketChannel) AlixHandler.SERVER_CHANNEL_FUTURE.channel();

            chField = AbstractNioChannel.class.getDeclaredField("ch");
            chField.setAccessible(true);

            delegate = (ServerSocketChannel) chField.get(serverSocketChannel);

            Class<?> serverChannelClass = delegate.getClass();//ServerSocketChannelImpl

            implCloseSelectableChannel = serverChannelClass.getDeclaredMethod("implCloseSelectableChannel");
            implConfigureBlocking = serverChannelClass.getDeclaredMethod("implConfigureBlocking", boolean.class);

            AlixServerSocketChannel interceptor = new AlixServerSocketChannel();

            //Unsafe unsafe = Unsafe.getUnsafe();

            for (Field f : AbstractSelectableChannel.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                AlixUnsafe.setFieldValue(f, interceptor, AlixUnsafe.getFieldValue(f, delegate));
            }

            chField.set(serverSocketChannel, interceptor);

        } catch (Exception e) {
            throw new AlixException(e);
        }
    }

    private static final class AlixServerSocketChannel extends ServerSocketChannel {

        private AlixServerSocketChannel() {
            super(delegate.provider());
        }

        @Override
        public SocketChannel accept() throws IOException {
            //Main.logError("ACCEPT INVOKED");
            SocketChannel channel = delegate.accept();
            if (channel == null) return channel;
            if (FireWallManager.isBlocked(((InetSocketAddress) channel.getRemoteAddress()))) {
                channel.close();
                return null;
            }
            return channel;
        }

        @Override
        public ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
            return delegate.bind(local, backlog);
        }

        @Override
        public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
            return delegate.setOption(name, value);
        }

        @Override
        public ServerSocket socket() {
            return delegate.socket();
        }

        @Override
        public SocketAddress getLocalAddress() throws IOException {
            return delegate.getLocalAddress();
        }

        @Override
        public void implCloseSelectableChannel() throws IOException {
            try {
                implCloseSelectableChannel.invoke(delegate);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void implConfigureBlocking(boolean block) throws IOException {
            try {
                implConfigureBlocking.invoke(delegate, block);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IOException(e);
            }
        }

        @Override
        public <T> T getOption(SocketOption<T> name) throws IOException {
            return delegate.getOption(name);
        }

        @Override
        public Set<SocketOption<?>> supportedOptions() {
            return delegate.supportedOptions();
        }
    }

    public static void unregister() {
        try {
            NioServerSocketChannel serverSocketChannel = (NioServerSocketChannel) AlixHandler.SERVER_CHANNEL_FUTURE.channel();
            chField.set(serverSocketChannel, delegate);//return the original socket
        } catch (IllegalAccessException e) {
            throw new AlixException(e);
        }
    }

    public static void init() {
    }
}