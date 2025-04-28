package alix.common.antibot.algorithms.any;

import java.net.InetAddress;

public interface ConnectionRequestAlgorithm {

    void onConnection(InetAddress ip);

    void onThreadRepeat();

}