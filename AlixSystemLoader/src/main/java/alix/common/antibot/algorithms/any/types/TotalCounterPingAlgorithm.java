/*
package alix.common.antibot.algorithms.any.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.any.ConnectionRequestAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public final class TotalCounterPingAlgorithm implements ConnectionRequestAlgorithm {

    private static final String ALGORITHM_ID = "D2";
    private static final AlixMessage consoleMessage = Messages.getAsObject("anti-ddos-fail-console-message", "{0}", ALGORITHM_ID);
    private final Map<InetAddress, AtomicInteger> map = new ConcurrentHashMap<>();
    private final BiFunction<InetAddress, AtomicInteger, AtomicInteger> function = (ip, i) -> {
        int n = i.get() - 20;
        if (n < -100) return null;
        if (n > 200) {
            if (FireWallManager.add(ip, ALGORITHM_ID))
                AlixCommonMain.logInfo(consoleMessage.format(ip));
            return null;
        }
        return i;
    };

    @Override
    public void onConnection(InetAddress ip) {
        //this.map.compute(ip, (a, i) -> i == null ? 1 : i + 1);
        //this.map.merge(ip, 1, (counter, one) -> counter + 1);
        this.map.computeIfAbsent(ip, i -> new AtomicInteger()).getAndIncrement();
    }

    @Override
    public void onThreadRepeat() {
        this.map.forEach((ip, i) -> this.map.compute(ip, this.function));
    }
}*/
