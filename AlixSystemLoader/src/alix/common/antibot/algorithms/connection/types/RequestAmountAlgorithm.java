/*
package alix.common.antibot.algorithms.connection.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.algorithms.connection.ConnectionAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class RequestAmountAlgorithm implements ConnectionAlgorithm {

    //private final String userMessage = Messages.get("anti-bot-fail-suspect-message","{0}", this.getAlgorithmID());
    private static final String ALGORITHM_ID = "A3";
    private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-message", "{0}", ALGORITHM_ID);
    private final Map<InetAddress, Integer> map = new ConcurrentHashMap<>();
    private final DecrementFunction function = new DecrementFunction();
    private final Integer INT_DEFAULT = 10;

    @Override
    public void onJoinAttempt(String name, InetAddress ip) {
        this.map.compute(ip, (address, count) -> count == null ? INT_DEFAULT : count + 1);
    }

    @Override
    public void onThreadRepeat() {
        this.map.forEach((ip, count) -> this.map.compute(ip, this.function));
    }

    @Override
    public String getAlgorithmID() {
        return ALGORITHM_ID;
    }

    private static final class DecrementFunction implements BiFunction<InetAddress, Integer, Integer> {

        @Override
        public final Integer apply(InetAddress ip, Integer count) {
            int diff = count - 1;
            if (diff == 0) return null;
            if (diff > 120) {
                if (FireWallManager.add(ip, ALGORITHM_ID))
                    AlixCommonMain.logInfo(consoleMessage.format(ip));
                return null;
            }
            return count;
        }
    }
}
*/
