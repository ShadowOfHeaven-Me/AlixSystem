package alix.common.antibot.algorithms.ping.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.ping.PingRequestAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class TotalCounterPingAlgorithm implements PingRequestAlgorithm {

    private static final String ALGORITHM_ID = "D1";
    private static final AlixMessage consoleMessage = Messages.getAsObject("anti-ddos-fail-console-message", "{0}", ALGORITHM_ID);
    private final Map<InetAddress, Integer> map = new ConcurrentHashMap<>();
    private final BiFunction<InetAddress, Integer, Integer> function = (ip, i) -> {
        int n = i - 25;
        if (n < -200) return null;
        if (n > 300) {
            if (FireWallManager.add(ip, ALGORITHM_ID))
                AlixCommonMain.logInfo(consoleMessage.format(ip));
            return null;
        }
        return i;
    };

    @Override
    public void onPingRequest(InetAddress ip) {
        this.map.compute(ip, (a, i) -> i == null ? 1 : i + 1);
    }

    @Override
    public void onThreadRepeat() {
        this.map.forEach((ip, i) -> this.map.compute(ip, this.function));
    }
}