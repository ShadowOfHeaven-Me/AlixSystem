package alix.common.data.fingerprinting;

import alix.common.AlixCommonMain;
import alix.common.utils.config.ConfigParams;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public final class FingerprintServer {

    private static volatile boolean started;

    static {
        if (ConfigParams.fingerprintingEnabled) {
            init();
        }
    }

    public static synchronized void init() {
        if (!started && ConfigParams.fingerprintingEnabled) {
            started = true;
            start0();
        }
    }

    private static void start0() {
        EventLoopGroup bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        var transport = Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
        int port = ConfigParams.fingerprintingPort;

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(transport)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            var pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new FingerprintHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            AlixCommonMain.logInfo("Fingerprinting listening on port " + port);

            f.channel().closeFuture().addListener(sex -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            });
        } catch (Throwable e) {
            AlixCommonMain.logWarning("Failed to start the Fingerprinting listener on " + port + ": " + e.getMessage());
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
