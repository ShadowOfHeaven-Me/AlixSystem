package alix.common.antibot.algorithms.connection;

import java.net.InetAddress;

public interface ConnectionAlgorithm {

    boolean onJoinAttempt(String name, InetAddress ip);

}