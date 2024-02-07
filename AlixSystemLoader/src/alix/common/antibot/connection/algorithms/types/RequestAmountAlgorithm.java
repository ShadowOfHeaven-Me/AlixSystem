package alix.common.antibot.connection.algorithms.types;

import alix.common.AlixCommonMain;
import alix.common.antibot.connection.algorithms.ConnectionAlgorithm;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.utils.other.AtomicFloat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class RequestAmountAlgorithm implements ConnectionAlgorithm {

    //private final String userMessage = Messages.get("anti-bot-fail-suspect-message","{0}", this.getAlgorithmID());
    private static final String ALGORITHM_ID = "A2";
    private final Map<String, AtomicFloat> map = new ConcurrentHashMap<>();
    private final DecrementFunction function = new DecrementFunction();

    @Override
    public void onJoinAttempt(String name, String ip) {
        this.map.compute(ip, (address, count) -> count == null ? new AtomicFloat(10) : count.incrementAndGetSelf());
    }

    @Override
    public void onThreadRepeat() {
        this.map.forEach((ip, count) -> this.map.compute(ip, this.function));
    }

    @Override
    public String getAlgorithmID() {
        return ALGORITHM_ID;
    }

    private static final class DecrementFunction implements BiFunction<String, AtomicFloat, AtomicFloat> {

        private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-message", "{0}", ALGORITHM_ID);

        @Override
        public final AtomicFloat apply(String ip, AtomicFloat count) {
            float diff = count.addAndGet(-1.75f);
            //CommonAlixMain.logInfo("IP: " + ip + " diff: " + diff);
            if (diff <= 0) return null;
            if (diff > 120) {
                //AlixScheduler.sync(() -> AbstractBanList.IP.ban(ip, userMessage, null, AlixFormatter.pluginPrefix));
                if (FireWallManager.add(ip, ALGORITHM_ID))
                    AlixCommonMain.logInfo(consoleMessage.format(ip));
                return null;
            }
            return count;
        }
    }
}