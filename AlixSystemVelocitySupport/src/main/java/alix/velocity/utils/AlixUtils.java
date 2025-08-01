package alix.velocity.utils;

import alix.common.logger.velocity.VelocityLoggerAdapter;
import alix.common.utils.formatter.AlixFormatter;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import io.netty.channel.Channel;
import io.netty.channel.local.LocalAddress;
import net.kyori.adventure.text.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class AlixUtils {

    public static void sendMessage(CommandSource source, String msg) {
        var str = AlixFormatter.colorize(msg);

        if (source instanceof ConsoleCommandSource)
            VelocityLoggerAdapter.sendMessage((ConsoleCommandSource) source, str);
        else source.sendMessage(Component.text(str));
    }

    public static InetAddress getAddress(Channel channel) {
        return getAddress(channel.remoteAddress());
    }

    public static InetAddress getAddress(SocketAddress address) {
        if (address instanceof LocalAddress) return null;
        return ((InetSocketAddress) address).getAddress();
    }

    public static void init() {
    }
}