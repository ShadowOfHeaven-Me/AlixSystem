package alix.common.utils.file.types;

import alix.common.antibot.IPUtils;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.other.throwable.AlixException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class IPsCacheFile extends AlixFileManager {

    private final Map<InetAddress, Boolean> map = new ConcurrentHashMap<>(1 << 7);//128

    public IPsCacheFile() {
        super("ips_cache.txt");
    }

    @Override
    protected void loadLine(String line) {
        String[] a = line.split("\\|");
        this.map.put(IPUtils.fromAddress(a[0]), a[1].equals("1"));
    }

    public void save() {
        try {
            super.saveKeyAndVal(this.map,"|", null, InetAddress::getHostAddress, b -> b ? "1" : "0");
        } catch (IOException e) {
            throw new AlixException(e);
        }
    }

    public Map<InetAddress, Boolean> getMap() {
        return map;
    }
}