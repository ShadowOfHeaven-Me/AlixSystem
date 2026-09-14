package alix.common.antibot.firewall.ataraxia;

import io.netty.channel.epoll.Epoll;

import java.net.InetAddress;

public final class AlixAtaraxia {

    static {
        init();
    }

    public static void blacklist(InetAddress ip) {
    }

    public static void whitelist(InetAddress ip) {

    }

    public static void unblacklist(String ip) {

    }

    public static void unwhitelist(InetAddress ip) {

    }

    public static boolean isEnabled() {
        return false;
    }

    private static void init() {
        if (!Epoll.isAvailable()) return;

        AtaraxiaIPC.start0();
    }
}