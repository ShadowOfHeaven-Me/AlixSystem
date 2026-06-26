package alix.velocity.utils;

import alix.common.logger.velocity.VelocityLoggerAdapter;
import alix.common.utils.formatter.AlixFormatter;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.kyori.adventure.text.Component;

public final class AlixUtils {

    public static void sendMessage(CommandSource source, String msg) {
        var str = AlixFormatter.colorize(msg);

        if (source instanceof ConsoleCommandSource)
            VelocityLoggerAdapter.sendMessage((ConsoleCommandSource) source, str);
        else source.sendMessage(Component.text(str));
    }

    public static void init() {
    }
}