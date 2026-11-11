package alix.common.logger.velocity;

import alix.common.logger.LoggerAdapter;
import alix.common.utils.formatter.AlixFormatter;
import alix.loaders.velocity.VelocityAlixMain;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class VelocityLoggerAdapter implements LoggerAdapter {

    private final ConsoleCommandSource console;
    private final String prefix;

    public VelocityLoggerAdapter(String prefix) {
        this.prefix = prefix;
        this.console = VelocityAlixMain.instance.getServer().getConsoleCommandSource();
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
        sendMessage(this.console, this.prefix + AlixFormatter.translateColors(msg));
    }

    @Override
    public void log(String log) {
        sendMessage(this.console, AlixFormatter.translateColors(log));
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