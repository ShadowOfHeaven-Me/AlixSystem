package alix.common.antibot.algorithms.connection.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class Name2IPAlgorithm implements ConnectionAlgorithm {

    //private final String userMessage = Messages.get("anti-bot-fail-suspect-message","{0}", this.getAlgorithmID());
    private static final String ALGORITHM_ID = "B2";
    private final Map<InetAddress, NameMapImpl> ipMap = new ConcurrentHashMap<>();//<ip, Name to join attempt map>

    @Override
    public void onJoinAttempt(String name, InetAddress ip) {
        if (name == null) return; //Does not support pre-login checks
        if (this.ipMap.computeIfAbsent(ip, NameMapImpl::new).add(name)) this.ipMap.remove(ip);
    }

    @Override
    public void onThreadRepeat() {
        //final long now = System.currentTimeMillis();
        this.ipMap.forEach((ip, map) -> {
            if (map.tick() <= 0) this.ipMap.remove(ip);
        });
    }

    @Override
    public String getAlgorithmID() {
        return ALGORITHM_ID;
    }

    private static final class NameMapImpl extends NameMap {

        private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-message", "{0}", ALGORITHM_ID);
        private final AtomicInteger vl;

        private NameMapImpl(InetAddress address) {
            super(address);
            this.vl = new AtomicInteger();
            //this.removalTime = new AtomicLong(System.currentTimeMillis() + 20000);
        }

        protected boolean add(String name) {
            //AliCommonMain.logInfo("IP: " + ip + " Name To IP: " + vl.get());

            if (this.namesSet.add(name)) this.vl.getAndAdd(24);
            else this.vl.getAndAdd(2);

            if (this.vl.get() > 240) {
                if (FireWallManager.add(ip, ALGORITHM_ID))
                    AlixCommonMain.logInfo(consoleMessage.format(ip.getHostAddress()));
                return true;
            }
            return false;
        }

        private int tick() {
            return this.vl.addAndGet(-3);
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