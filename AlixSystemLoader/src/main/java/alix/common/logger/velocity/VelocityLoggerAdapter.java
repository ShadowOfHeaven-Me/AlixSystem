package alix.common.logger.velocity;

import alix.common.logger.LoggerAdapter;
import alix.common.utils.formatter.AlixFormatter;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class VelocityLoggerAdapter implements LoggerAdapter {

    private final ConsoleCommandSource console;

    public VelocityLoggerAdapter(ProxyServer server) {
        this.console = server.getConsoleCommandSource();
    }

    public static void sendMessage(ConsoleCommandSource source, String msg) {
        System.out.println(toAnsi(msg));
    }

    private static String toAnsi(String input) {
        // Deserialize legacy §-coded text into a Component
        Component component = LegacyComponentSerializer.legacySection().deserialize(input);
        // Serialize the Component into ANSI escape sequences
        return ANSIComponentSerializer.ansi().serialize(component);
    }

    private void sendMessage(String msg) {
        sendMessage(this.console, AlixFormatter.colorize(msg));
    }

    @Override
    public void info(String info) {
        this.sendMessage(info);
    }

    @Override
    public void warning(String warning) {
        this.sendMessage("&e[WARNING] > " + warning);
    }

    @Override
    public void error(String error) {
        this.sendMessage("&c[ERROR] > " + error);
    }

    @Override
    public void debug(String debug) {
        this.sendMessage("&b[DEBUG] > " + debug);
    }
}