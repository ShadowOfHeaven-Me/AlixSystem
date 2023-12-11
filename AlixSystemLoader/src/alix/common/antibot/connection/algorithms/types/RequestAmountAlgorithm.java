package alix.common.antibot.connection.algorithms.types;

import alix.common.CommonAlixMain;
import alix.common.antibot.connection.algorithms.ConnectionAlgorithm;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.impl.AlixScheduler;
import alix.common.utils.formatter.AlixFormatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class RequestAmountAlgorithm implements ConnectionAlgorithm {

    private final Map<String, Integer> map = new ConcurrentHashMap<>();
    private final DecrementFunction function = new DecrementFunction();

    @Override
    public void onJoinAttempt(String name, String ip) {
        this.map.compute(ip, (address, count) -> count == null ? 1 : count + 1);
    }

    @Override
    public void onThreadRepeat() {
        this.map.forEach((ip, count) -> this.map.compute(ip, this.function));
    }

    private static final class DecrementFunction implements BiFunction<String, Integer, Integer> {

        private static final String userMessage = Messages.get("anti-bot-fail-requests");
        private static final AlixMessage consoleMessage = Messages.getAsObject("anti-bot-fail-console-requests");

        @Override
        public final Integer apply(String ip, Integer count) {
            CommonAlixMain.logInfo("IP: " + ip + " count " + (count - 1));
            if (count == 1) return null;
            if (count > 30) {
                AlixScheduler.sync(() -> ipBanList.addBan(ip, userMessage, null, AlixFormatter.pluginPrefix));
                CommonAlixMain.logInfo(consoleMessage.format(ip));
                return null;
            }
            return count - 1;
        }
    }
}