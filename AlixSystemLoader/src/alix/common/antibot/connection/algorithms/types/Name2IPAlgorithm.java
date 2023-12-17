package alix.common.antibot.connection.algorithms.types;

import alix.common.CommonAlixMain;
import alix.common.antibot.connection.algorithms.ConnectionAlgorithm;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.impl.AlixScheduler;
import alix.common.utils.formatter.AlixFormatter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class Name2IPAlgorithm implements ConnectionAlgorithm {

    private final Map<String, NameMap> ipMap = new ConcurrentHashMap<>();//<ip, Name to join attempt map>

    @Override
    public void onJoinAttempt(String name, String ip) {
        if (this.ipMap.computeIfAbsent(ip, NameMap::new).add(name)) this.ipMap.remove(ip);
    }

    @Override
    public void onThreadRepeat() {
        final long now = System.currentTimeMillis();
        this.ipMap.forEach((ip, map) -> {
            if (map.removalTime.get() < now) this.ipMap.remove(ip);
        });
    }

    private static final class NameMap {

        private static final String userMessage = Messages.get("anti-bot-fail-name-to-ip");
        private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-name-to-ip");
        private final Set<String> names;
        private final String ip;
        private final AtomicInteger vl;
        private final AtomicLong removalTime;

        private NameMap(String address) {
            this.names = new HashSet<>();
            this.ip = address;
            this.vl = new AtomicInteger();
            this.removalTime = new AtomicLong(System.currentTimeMillis() + 20000);
        }

        private boolean add(String name) {
            CommonAlixMain.logInfo("IP: " + ip + " Name To IP: " + vl.get());

            if (this.names.add(name)) {
                this.vl.getAndAdd(8);
                this.removalTime.getAndAdd(15000);
            } else {
                this.vl.getAndIncrement();
                this.removalTime.getAndAdd(2000);
            }


            if (this.vl.get() > 50) {
                AlixScheduler.sync(() -> ipBanList.addBan(ip, userMessage, null, AlixFormatter.pluginPrefix));
                CommonAlixMain.logInfo(consoleMessage.format(ip));
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