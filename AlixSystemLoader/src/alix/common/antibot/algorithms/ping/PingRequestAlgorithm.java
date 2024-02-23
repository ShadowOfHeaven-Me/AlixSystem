package alix.common.antibot.algorithms.ping;

import java.net.InetAddress;

public interface PingRequestAlgorithm {

    void onPingRequest(InetAddress ip);

    void onThreadRepeat();

}