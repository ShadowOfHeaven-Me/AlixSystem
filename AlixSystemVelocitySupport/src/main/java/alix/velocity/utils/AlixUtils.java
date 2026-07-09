package alix.velocity.utils;

import alix.common.logger.velocity.VelocityLoggerAdapter;
import alix.common.utils.formatter.AlixFormatter;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.proxy.network.Connections;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;

public final class AlixUtils {

    public static void sendMessage(CommandSource source, String msg) {
        var str = AlixFormatter.colorize(msg);

        if (source instanceof ConsoleCommandSource)
            VelocityLoggerAdapter.sendMessage((ConsoleCommandSource) source, str);
        else source.sendMessage(Component.text(str));
    }

    public static Boolean isOnlineEncryptionEnabled(Channel channel) {
        return channel == null ? null : channel.pipeline().context(Connections.CIPHER_ENCODER) != null;
    }

    public static void init() {
    }
}