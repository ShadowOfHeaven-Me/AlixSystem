package shadow.utils.users.types;

import alix.spigot.api.users.AlixSpigotUser;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.netty.NettyUtils;

abstract class AbstractAlixCtxUser implements AlixUser, AlixSpigotUser {

    private volatile ChannelHandlerContext silentContext;

    AbstractAlixCtxUser(ChannelHandlerContext silentContext) {
        this.silentContext = silentContext;
    }

    @Override
    public final Channel getChannel() {
        return this.silentContext.channel();
    }

    @Override
    public final ChannelHandlerContext silentContext() {
        return this.silentContext.isRemoved() ? (this.silentContext = NettyUtils.getSilentContext(this.getChannel())) : this.silentContext;
    }
}