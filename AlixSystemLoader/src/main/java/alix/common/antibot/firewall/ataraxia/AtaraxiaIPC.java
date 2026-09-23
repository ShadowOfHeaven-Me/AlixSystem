package alix.common.antibot.firewall.ataraxia;

import alix.common.AlixCommonMain;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;

import java.io.File;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static alix.common.antibot.firewall.ataraxia.AtaraxiaProtocol.encodeJ2RMapUpdate;

final class AtaraxiaIPC {

    private static final Logger LOGGER = Logger.getLogger("AlixAtaraxia");
    private static final File ATARAXIA_FOLDER, SOCKET_FILE;
    //AF_UNIX socket paths are capped at 108 bytes on Linux (sun_path); bind() fails with
    //"File name too long" past that. Falls back to a short path under the system temp dir, keyed by a hash
    //of the natural path so it's still per-install-unique.
    private static final int MAX_SOCKET_PATH_BYTES = 100;

    static {
        ATARAXIA_FOLDER = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolder(), "ataraxia");

        File ipcFolder = new File(ATARAXIA_FOLDER, "ipc");
        ipcFolder.mkdirs();

        File naturalSocketFile = new File(ipcFolder, "ipc.sock");
        SOCKET_FILE = naturalSocketFile.getAbsolutePath().getBytes(StandardCharsets.UTF_8).length >= MAX_SOCKET_PATH_BYTES
                ? new File(System.getProperty("java.io.tmpdir"), "alix-" + Integer.toHexString(naturalSocketFile.getAbsolutePath().hashCode()) + ".sock")
                : naturalSocketFile;
    }

    static void syncAll(ServerHandler handler) {
        handler.write(encodeJ2RMapUpdate(false, true, GeoIPTracker.EXISTING_ACCOUNTS.keySet()));

        FireWallManager.AT_LOAD_COMPLETE.thenRun(() -> {
            handler.write(encodeJ2RMapUpdate(true, true, FireWallManager.staticBlockedSet()));
            handler.writeAndFlush(encodeJ2RMapUpdate(true, true, FireWallManager.dynamicBlockedSet()));
        });
    }

    static void mapUpdate_writeAndFlush(boolean isBlacklist, boolean add, Collection<InetAddress> ips) {
        ServerHandler.INSTANCE.writeAndFlush(encodeJ2RMapUpdate(isBlacklist, add, ips));
    }

    //Must never let a bind failure escape - this runs from AlixAtaraxia's static initializer, reachable via
    //an isEnabled()-style check from arbitrary call sites (connection handling, admin commands); an
    //uncaught exception there previously aborted whichever unrelated operation happened to trigger it.
    static void start0() {
        SOCKET_FILE.delete();

        EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
        EventLoopGroup workerGroup = new EpollEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(EpollServerDomainSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(ServerHandler.INSTANCE);
                        }
                    });

            ChannelFuture f = b.bind(new DomainSocketAddress(SOCKET_FILE)).sync();
            LOGGER.info("Listening on " + SOCKET_FILE);

            f.channel().closeFuture().addListener(sex -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                SOCKET_FILE.delete();
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interrupted while starting the Ataraxia IPC listener on " + SOCKET_FILE, e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to start the Ataraxia IPC listener on " + SOCKET_FILE, e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}