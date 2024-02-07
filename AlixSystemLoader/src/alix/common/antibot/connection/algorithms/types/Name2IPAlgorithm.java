package alix.common.antibot.connection.algorithms.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.connection.algorithms.ConnectionAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.utils.other.AtomicFloat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Name2IPAlgorithm implements ConnectionAlgorithm {

    //private final String userMessage = Messages.get("anti-bot-fail-suspect-message","{0}", this.getAlgorithmID());

    private static final String ALGORITHM_ID = "B2";
    private final Map<String, NameMapImpl> ipMap = new ConcurrentHashMap<>();//<ip, Name to join attempt map>

    @Override
    public void onJoinAttempt(String name, String ip) {
        if (this.ipMap.computeIfAbsent(ip, NameMapImpl::new).add(name)) this.ipMap.remove(ip);
    }

    @Override
    public void onThreadRepeat() {
        //final long now = System.currentTimeMillis();
        this.ipMap.forEach((ip, map) -> {
            if (map.vl.addAndGet(-1.5f) <= 0) this.ipMap.remove(ip);
        });
    }

    @Override
    public String getAlgorithmID() {
        return ALGORITHM_ID;
    }

    private static final class NameMapImpl extends NameMap {

        private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-message", "{0}", ALGORITHM_ID);
        private final AtomicFloat vl;

        private NameMapImpl(String address) {
            super(address);
            this.vl = new AtomicFloat();
            //this.removalTime = new AtomicLong(System.currentTimeMillis() + 20000);
        }

        protected boolean add(String name) {
            //AliCommonMain.logInfo("IP: " + ip + " Name To IP: " + vl.get());

            if (this.namesSet.add(name)) this.vl.getAndAdd(12);
            else this.vl.incrementAndGetSelf();

            if (this.vl.get() > 120) {
                if (FireWallManager.add(ip, ALGORITHM_ID))
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