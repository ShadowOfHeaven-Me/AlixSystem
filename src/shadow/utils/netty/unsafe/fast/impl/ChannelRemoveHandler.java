package shadow.utils.netty.unsafe.fast.impl;

import io.netty.channel.Channel;

public interface ChannelRemoveHandler {

    void remove(Channel channel, String handlerName);

    static ChannelRemoveHandler createImpl() {
        try {
            return new FastChannelRemove();
        } catch (Exception e) {
            return (channel, handlerName) -> channel.pipeline().remove(handlerName);
        }
    }
}
