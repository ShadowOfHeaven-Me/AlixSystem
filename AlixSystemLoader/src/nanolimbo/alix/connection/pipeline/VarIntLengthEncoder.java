/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package nanolimbo.alix.connection.pipeline;

import alix.common.utils.netty.BufUtils;
import alix.common.utils.netty.FastNettyUtils;
import io.netty.buffer.ByteBuf;

final class VarIntLengthEncoder {// extends MessageToByteEncoder<ByteBuf> {

    static ByteBuf encode(ByteBuf msg, boolean pooled) {
        int readableBytes = msg.readableBytes();
        byte varIntBytes = FastNettyUtils.countBytesToEncodeVarInt(readableBytes);

        //prefix the buf with the VarInt length of it's byte length
        int newCapacity = readableBytes + varIntBytes;
        ByteBuf out = pooled ? BufUtils.pooledBuffer(newCapacity) : BufUtils.unpooledBuffer(newCapacity); //Unpooled.directBuffer(msg.capacity() + varIntBytes);

        FastNettyUtils.writeVarInt(out, readableBytes, varIntBytes);
        out.writeBytes(msg);

        msg.release();

        return out;
    }

    /*@Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf out) {
        ByteMessage msg = new ByteMessage(out);
        msg.writeVarInt(buf.readableBytes());
        msg.writeBytes(buf);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
        int anticipatedRequiredCapacity = 5 + msg.readableBytes();
        return ctx.alloc().heapBuffer(anticipatedRequiredCapacity);
    }*/
}
