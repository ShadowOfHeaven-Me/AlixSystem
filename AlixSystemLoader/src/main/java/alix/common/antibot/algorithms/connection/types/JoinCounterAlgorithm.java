package alix.common.antibot.algorithms.connection.types;

import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.firewall.AlgorithmId;
import alix.common.antibot.firewall.FireWallManager;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static alix.common.antibot.firewall.AlgorithmId.C1;

public final class JoinCounterAlgorithm implements ConnectionAlgorithm {

    private static final AlgorithmId ALGORITHM_ID = C1;
    private final Map<InetAddress, NameMapImpl> ipMap = new ConcurrentHashMap<>();

    @Override
    public void onJoinAttempt(String name, InetAddress ip) {
        if (name == null) return; //Does not support pre-login checks
        if (this.ipMap.computeIfAbsent(ip, NameMapImpl::new).add(name)) this.ipMap.remove(ip);
    }

    @Override
    public void onThreadRepeat() {
        long now = System.currentTimeMillis();
        this.ipMap.forEach((ip, map) -> {
            if (map.removalTime.get() < now) this.ipMap.remove(ip);
        });
    }

    @Override
    public AlgorithmId getAlgorithmID() {
        return ALGORITHM_ID;
    }


    private static final class NameMapImpl extends NameMap {

        private final AtomicLong removalTime;

        private NameMapImpl(InetAddress address) {
            super(address);
            this.removalTime = new AtomicLong(System.currentTimeMillis() + 15000);
        }

        protected boolean add(String name) {
            //AliCommonMain.logInfo("IP: " + ip + " Name To IP: " + vl.get());

            if (this.namesSet.add(name)) this.removalTime.getAndAdd(7000);
            else this.removalTime.getAndAdd(500);

            if (this.namesSet.size() > 12) {
                FireWallManager.add(ip, ALGORITHM_ID, true);
                return true;
            }
            return false;
        }
    }
}