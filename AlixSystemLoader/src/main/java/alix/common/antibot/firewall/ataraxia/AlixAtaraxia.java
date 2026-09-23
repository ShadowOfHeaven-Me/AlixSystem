package alix.common.antibot.firewall.ataraxia;

import io.netty.channel.epoll.Epoll;

import java.net.InetAddress;
import java.util.List;

public final class AlixAtaraxia {

    //A compile-time constant (unlike a method call, reading this field never triggers this class's static
    //initializer in a caller - see JLS 12.4.1) so every isEnabled()-style check elsewhere in the codebase
    //can gate a call into this class without itself starting the IPC listener below as a side effect.
    public static final boolean ENABLED = false;

    static {
        if (ENABLED) init();
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

    private static void init() {
        if (!Epoll.isAvailable()) return;

        AtaraxiaIPC.start0();
    }
}