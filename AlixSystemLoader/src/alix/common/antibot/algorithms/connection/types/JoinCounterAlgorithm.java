package alix.common.antibot.algorithms.connection.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class JoinCounterAlgorithm implements ConnectionAlgorithm {

    private static final String ALGORITHM_ID = "C1";
    private final Map<String, NameMapImpl> ipMap = new ConcurrentHashMap<>();

    @Override
    public void onJoinAttempt(String name, String ip) {
        if (this.ipMap.computeIfAbsent(ip, NameMapImpl::new).add(name)) this.ipMap.remove(ip);
    }

    @Override
    public void onThreadRepeat() {
        final long now = System.currentTimeMillis();
        this.ipMap.forEach((ip, map) -> {
            if (map.removalTime < now) this.ipMap.remove(ip);
        });
    }

    @Override
    public String getAlgorithmID() {
        return ALGORITHM_ID;
    }

    /** @noinspection NonAtomicOperationOnVolatileField*/
    private static final class NameMapImpl extends NameMap {

        private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-message", "{0}", ALGORITHM_ID);
        private volatile long removalTime;

        private NameMapImpl(String address) {
            super(address);
            this.removalTime = System.currentTimeMillis() + 15000;
        }

        protected boolean add(String name) {
            //AliCommonMain.logInfo("IP: " + ip + " Name To IP: " + vl.get());

            if (this.namesSet.add(name)) this.removalTime += 7000;
            else this.removalTime += 500;

            if (this.namesSet.size() > 12) {
                if (FireWallManager.add(ip, ALGORITHM_ID))
                    AlixCommonMain.logInfo(consoleMessage.format(ip));
                return true;
            }
            return false;
        }
    }

}