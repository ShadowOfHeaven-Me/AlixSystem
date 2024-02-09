package alix.common.antibot.algorithms.ping;

public interface PingRequestAlgorithm {

    void onPingRequest(String ip);

    void onThreadRepeat();

}