package alix.common.utils.file.managers;

import alix.common.utils.file.types.IPsCacheFile;

import java.net.InetAddress;

public final class IpsCacheFileManager {

    private static final IPsCacheFile file = new IPsCacheFile();

    public static void add(InetAddress ip, boolean isProxy) {
        file.getMap().put(ip, isProxy);
        //add(ip.getHostAddress(), isProxy);
    }

    public static Boolean isProxy(InetAddress ip) {
        return file.getMap().get(ip);
    }

    /*public static void add(String ip, boolean isProxy) {
        file.getMap().put(ip, isProxy);
    }*/

    public static void save() {
        file.save();
    }

    static {
        file.loadExceptionless();
    }

    public static void init() {
    }
}