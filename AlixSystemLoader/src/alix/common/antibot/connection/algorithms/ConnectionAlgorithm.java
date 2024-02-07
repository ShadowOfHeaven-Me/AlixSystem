package alix.common.antibot.connection.algorithms;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface ConnectionAlgorithm {

    void onJoinAttempt(String name, String ip);

    void onThreadRepeat();

    String getAlgorithmID();

    abstract class NameMap {

        protected final Set<String> namesSet;
        protected final String ip;

        protected NameMap(String address) {
            this.namesSet = ConcurrentHashMap.newKeySet();
            this.ip = address;
        }

        protected abstract boolean add(String name);
    }
}