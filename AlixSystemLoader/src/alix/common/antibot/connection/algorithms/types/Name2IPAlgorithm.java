package alix.common.antibot.connection.algorithms.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.connection.algorithms.ConnectionAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.utils.other.AtomicFloat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Name2IPAlgorithm implements ConnectionAlgorithm {

    //private final String userMessage = Messages.get("anti-bot-fail-suspect-message","{0}", this.getAlgorithmID());
    private final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-message","{0}", this.getAlgorithmID());
    private final Map<String, NameMap> ipMap = new ConcurrentHashMap<>();//<ip, Name to join attempt map>

    @Override
    public void onJoinAttempt(String name, String ip) {
        if (this.ipMap.computeIfAbsent(ip, NameMap::new).add(name)) this.ipMap.remove(ip);
    }

    @Override
    public void onThreadRepeat() {
        //final long now = System.currentTimeMillis();
        this.ipMap.forEach((ip, map) -> {
            if (map.vl.addAndGet(-1.2f) <= 0) this.ipMap.remove(ip);
        });
    }

    @Override
    public String getAlgorithmID() {
        return "B1";
    }

    private final class NameMap {

        private final Set<String> names;
        private final String ip;
        private final AtomicFloat vl;
        //private final AtomicLong removalTime;

        private NameMap(String address) {
            this.names = new HashSet<>();
            this.ip = address;
            this.vl = new AtomicFloat();
            //this.removalTime = new AtomicLong(System.currentTimeMillis() + 20000);
        }

        private boolean add(String name) {
            //AliCommonMain.logInfo("IP: " + ip + " Name To IP: " + vl.get());

            if (this.names.add(name)) {
                this.vl.getAndAdd(12);
                //this.removalTime.getAndAdd(15000);
            } else {
                this.vl.incrementAndGetSelf();
                //this.removalTime.getAndAdd(2000);
            }


            if (this.vl.get() > 120) {
                //AlixScheduler.sync(() -> AbstractBanList.IP.ban(ip, userMessage, null, AlixFormatter.pluginPrefix));
                FireWallManager.add(ip, getAlgorithmID());
                AlixCommonMain.logInfo(consoleMessage.format(ip));
                return true;
            }
            return false;
        }
    }

/*    private static final class CheckFunction implements BiFunction<String, Integer, Integer> {
        private final String ip;

        private CheckFunction(String ip) {
            this.ip = ip;
        }

        @Override
        public Integer apply(String name, Integer vl) {
            if (vl == null) return 3;

            return vl + 1;
        }
    }*/
}