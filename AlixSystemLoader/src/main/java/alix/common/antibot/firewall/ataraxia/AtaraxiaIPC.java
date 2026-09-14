package alix.common.antibot.firewall.ataraxia;

import alix.common.AlixCommonMain;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import lombok.SneakyThrows;

import java.io.File;

import static alix.common.antibot.firewall.ataraxia.AtaraxiaProtocol.encodeJ2RMapUpdate;

final class AtaraxiaIPC {

    private static final File ATARAXIA_FOLDER, SOCKET_FILE;

    static {
        ATARAXIA_FOLDER = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolder(), "ataraxia");

        File ipcFolder = new File(ATARAXIA_FOLDER, "ipc");
        ipcFolder.mkdirs();

        SOCKET_FILE = new File(ipcFolder, "ipc.sock");
    }

    static void syncAll(ServerHandler handler) {
        handler.write(encodeJ2RMapUpdate(false, true, GeoIPTracker.EXISTING_ACCOUNTS.keySet()));

        FireWallManager.AT_LOAD_COMPLETE.thenRun(() -> {
            handler.write(encodeJ2RMapUpdate(true, true, FireWallManager.staticBlockedSet()));
            handler.writeAndFlush(encodeJ2RMapUpdate(true, true, FireWallManager.dynamicBlockedSet()));
        });
    }

    @SneakyThrows
    static void start0() {
        SOCKET_FILE.delete();

        EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
        EventLoopGroup workerGroup = new EpollEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(EpollServerDomainSocketChannel.class)
                .childHandler(new ChannelInitializer<EpollSocketChannel>() {
                    @Override
                    protected void initChannel(EpollSocketChannel ch) {
                        ch.pipeline().addLast(ServerHandler.INSTANCE);
                    }
                });

        ChannelFuture f = b.bind(new DomainSocketAddress(SOCKET_FILE)).sync();
        AlixCommonMain.logWarning("Java server listening on " + SOCKET_FILE);

        f.channel().closeFuture().addListener(sex -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            SOCKET_FILE.delete();
        });
    }
}