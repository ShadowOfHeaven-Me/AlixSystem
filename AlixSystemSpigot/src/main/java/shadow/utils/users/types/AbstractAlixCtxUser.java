package shadow.utils.users.types;

import alix.spigot.api.users.AlixSpigotUser;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.netty.NettyUtils;

abstract class AbstractAlixCtxUser implements AlixUser, AlixSpigotUser {

    //Captured once, independently of silentContext: a Channel stays a valid identity for the whole
    //connection, while a ChannelHandlerContext becomes removed once its handler leaves the pipeline (e.g.
    //on disconnect). getChannel() used to read this.silentContext.channel() directly, so once silentContext
    //was ever null - e.g. NettyUtils.getSilentContext() legitimately returns null when the packetevents
    //encoder is already gone from a closing channel's pipeline, and silentContext()'s recovery branch below
    //caches that null right back into the field - every later getChannel() call threw exactly this class's
    //name and "silentContext is null" (seen live: a VerifiedUser constructed from an UnverifiedUser whose
    //connection was already closing when silentContext() was read for the handoff).
    private final Channel channel;
    private volatile ChannelHandlerContext silentContext;

    AbstractAlixCtxUser(Channel channel, ChannelHandlerContext silentContext) {
        this.channel = channel;
        this.silentContext = silentContext;
    }

    @Override
    public final Channel getChannel() {
        return this.channel;
    }

    @Override
    public final ChannelHandlerContext silentContext() {
        return this.silentContext == null || this.silentContext.isRemoved() ? (this.silentContext = NettyUtils.getSilentContext(this.channel)) : this.silentContext;
    }
}