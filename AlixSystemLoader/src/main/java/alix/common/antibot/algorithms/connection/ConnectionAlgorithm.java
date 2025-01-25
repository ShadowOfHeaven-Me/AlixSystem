package alix.common.antibot.algorithms.connection;

import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface ConnectionAlgorithm {

    void onJoinAttempt(String name, InetAddress ip);

    void onThreadRepeat();

    String getAlgorithmID();

    abstract class NameMap {

        protected final Set<String> namesSet;
        protected final InetAddress ip;

        protected NameMap(InetAddress address) {
            this.namesSet = ConcurrentHashMap.newKeySet();
            this.ip = address;
        }

        protected abstract boolean add(String name);
    }
}