/*
package shadow.utils.netty.packets;

import alix.common.utils.netty.BufTransformer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.netty.unsafe.UnsafeNettyUtils;
import shadow.utils.users.types.AlixUser;

final class ConstRawAlixPacket extends AbstractAlixPacket {

    //private final AtomicBoolean initOccurred = new AtomicBoolean();
    private final ByteBuf[] nonRawBufs;
    private volatile ChannelHandlerContext ctxCache;

    ConstRawAlixPacket(ByteBuf[] bufs) {
        super(new ByteBuf[bufs.length]);
        this.nonRawBufs = bufs;
    }

    @Override
    public void write(AlixUser user) {
        ChannelHandlerContext ctx = user.silentContext();

        //init the correct raw bufs if contexts do not match
        if (this.ctxCache != ctx) {
            //release the previous, now outdated bufs
            if (this.ctxCache != null) {
                super.release0();
            }

            UnsafeNettyUtils.sendAndSetRaw(ctx, user.bufHarvester(), BufTransformer.CONST, this.nonRawBufs, this.bufs);
            this.ctxCache = ctx;
        }

        this.forEach(user::writeRaw);
    }

    @Override
    public boolean isConst() {
        return true;
    }

    @Override
    public void release0() {
        //no init occurred
        if (this.ctxCache == null) return;
        super.release0();
    }
}*/
