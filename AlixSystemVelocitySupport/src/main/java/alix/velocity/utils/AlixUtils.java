package alix.velocity.utils;

import alix.common.logger.velocity.VelocityLoggerAdapter;
import alix.common.packets.message.MessageWrapper;
import alix.common.utils.formatter.AlixFormatter;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.proxy.network.Connections;
import io.netty.channel.Channel;

public final class AlixUtils {

    //Was Component.text(str) - that treats the already-color-translated 'str' as flat, unparsed text, so a
    //"&#RRGGBB" hex code in a message ended up as literal '§x§...' characters instead of an actual color.
    //parseLegacy() is the same hex-aware Adventure legacy parser used for pre-login chat messages (see
    //PacketPlayOutMessage#withMessage()) - this is the central sendMessage() virtually every Velocity-side
    //command routes through, so this one fix covers the vast majority of post-login player-facing messages.
    public static void sendMessage(CommandSource source, String msg) {
        var str = AlixFormatter.colorize(msg);

        if (source instanceof ConsoleCommandSource)
            VelocityLoggerAdapter.sendMessage((ConsoleCommandSource) source, str);
        else source.sendMessage(MessageWrapper.parseLegacy(str));
    }

    public static Boolean isOnlineEncryptionEnabled(Channel channel) {
        return channel == null ? null : channel.pipeline().context(Connections.CIPHER_ENCODER) != null;
    }

    public static void init() {
    }
}