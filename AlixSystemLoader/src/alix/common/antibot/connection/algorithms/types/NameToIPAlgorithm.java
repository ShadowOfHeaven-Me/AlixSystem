package alix.common.antibot.connection.algorithms.types;

import alix.common.CommonAlixMain;
import alix.common.antibot.connection.algorithms.ConnectionAlgorithm;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.impl.AlixScheduler;
import alix.common.utils.formatter.AlixFormatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class NameToIPAlgorithm implements ConnectionAlgorithm {

    private final Map<String, NameMap> ipMap = new ConcurrentHashMap<>();//<ip, Name to join attempt map>

    @Override
    public void onJoinAttempt(String name, String ip) {
        this.ipMap.computeIfAbsent(ip, NameMap::new).add(name);
    }

    @Override
    public void onThreadRepeat() {
        long now = System.currentTimeMillis();
        this.ipMap.forEach((ip, map) -> {
            if (map.removalTime < now) map.decrement(now);
            else this.ipMap.remove(ip);
        });
    }

    private static final class NameMap {

        private static final String userMessage = Messages.get("anti-bot-fail-name-to-ip");
        private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-name-to-ip");
        private final Map<String, Long> nameMap; //<name, removal time of this particular name>
        private final String ip;
        private final AtomicInteger vl;
        private volatile long removalTime;

        private NameMap(String address) {
            this.nameMap = new ConcurrentHashMap<>();
            this.ip = address;
            this.vl = new AtomicInteger();
        }

        private void add(String name) {
            CommonAlixMain.logInfo("IP: " + ip + " Name To IP: " + vl.get());

            this.removalTime = System.currentTimeMillis() + 30000;//the removal time of this NameMap object after inactivity

            this.nameMap.compute(name, (n, removalTime) -> {
                if (removalTime == null) {//a new account tried to join
                    this.vl.getAndAdd(3);//add more violations (anticheat terminology)
                    return System.currentTimeMillis() + 30000;//the time on which this name will be forgotten if no more join attempts are made
                }
                this.vl.getAndIncrement();//increment if the account tried to join before
                return removalTime;//keep it the same
            });

            if (this.vl.get() > 50) {
                AlixScheduler.sync(() -> ipBanList.addBan(ip, userMessage, null, AlixFormatter.pluginPrefix));
                CommonAlixMain.logInfo(consoleMessage.format(ip));
            }
        }

        private void decrement(long now) {
            this.nameMap.forEach((name, count) -> this.nameMap.compute(name, (n, removalTime) -> removalTime < now ? null : removalTime));

            if (vl.get() > 0) vl.getAndAdd(-1);
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