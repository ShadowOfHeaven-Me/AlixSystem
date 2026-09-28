package alix.common.antibot.firewall.ataraxia;

import io.netty.channel.epoll.Epoll;

import java.net.InetAddress;
import java.util.List;

public final class AlixAtaraxia {

    static {
        init0();
    }

    public static void blacklist(InetAddress ip) {
        AtaraxiaIPC.mapUpdate_writeAndFlush(true, true, List.of(ip));
    }

    public static void whitelist(InetAddress ip) {
        AtaraxiaIPC.mapUpdate_writeAndFlush(false, true, List.of(ip));
    }

    public static void unblacklist(InetAddress ip) {
        AtaraxiaIPC.mapUpdate_writeAndFlush(true, false, List.of(ip));
    }

    public static void unwhitelist(InetAddress ip) {
        AtaraxiaIPC.mapUpdate_writeAndFlush(false, false, List.of(ip));
    }

    public static boolean isEnabled() {
        return AtaraxiaServerHandler.isConnected();
    }

    private static void init0() {
        if (!Epoll.isAvailable()) return;

        AtaraxiaIPC.start0();
    }

    public static void init() {
    }
}