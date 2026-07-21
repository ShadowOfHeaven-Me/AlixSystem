package alix.common.connection;

import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.algorithms.connection.types.JoinCounterAlgorithm;
import alix.common.antibot.algorithms.connection.types.Name2IPAlgorithm;

import java.net.InetAddress;

public final class ConnectionAlgoManager {

    static final ConnectionAlgorithm[] connectionAlgorithms = new ConnectionAlgorithm[]{
            new Name2IPAlgorithm(),
            new JoinCounterAlgorithm()
            //new RequestAmountAlgorithm()
    };

    public static boolean onJoinAttempt(String name, InetAddress address) {
        for (ConnectionAlgorithm algorithm : connectionAlgorithms)
            if (algorithm.onJoinAttempt(name, address))
                return true;
        return false;
    }

    private ConnectionAlgoManager() {
    }
}