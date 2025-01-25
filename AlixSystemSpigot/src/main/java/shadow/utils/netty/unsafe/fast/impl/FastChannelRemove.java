package shadow.utils.netty.unsafe.fast.impl;

import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelPipeline;
import shadow.utils.misc.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

final class FastChannelRemove implements ChannelRemoveHandler {

    private static final MethodHandle removeFromHandlerList;

    static {
        try {
            //Class<?> abstractChannelHandlerContextClazz = Class.forName("io.netty.channel.AbstractChannelHandlerContext");
            Method method = ReflectionUtils.getDeclaredMethod(DefaultChannelPipeline.class, "atomicRemoveFromHandlerList", "remove0");

            if (method == null) throw new RuntimeException();

            removeFromHandlerList = MethodHandles.privateLookupIn(DefaultChannelPipeline.class, MethodHandles.lookup()).unreflect(method);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Channel channel, String handlerName) {
        try {
            removeFromHandlerList.invoke(channel, channel.pipeline().context(handlerName));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}