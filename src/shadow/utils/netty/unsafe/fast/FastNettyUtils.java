package shadow.utils.netty.unsafe.fast;

import io.netty.channel.Channel;
import shadow.utils.netty.unsafe.fast.impl.ChannelRemoveHandler;


public final class FastNettyUtils {

    private static final ChannelRemoveHandler removeHandler = ChannelRemoveHandler.createImpl();

    public static void remove(Channel channel, String handlerName) {
        removeHandler.remove(channel, handlerName);
    }
}