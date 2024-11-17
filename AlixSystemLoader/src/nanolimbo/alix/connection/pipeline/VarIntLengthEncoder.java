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

import alix.common.utils.netty.FastNettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import nanolimbo.alix.protocol.ByteMessage;

public final class VarIntLengthEncoder extends MessageToByteEncoder<ByteBuf> {

    static ByteBuf encode(ByteMessage msg) {
        int toEncode = msg.readableBytes();
        byte varIntBytes = FastNettyUtils.countBytesToEncodeVarInt(toEncode);

        ByteBuf prefixedBuf = Unpooled.directBuffer(msg.capacity() + varIntBytes);
        FastNettyUtils.writeVarInt(prefixedBuf, toEncode, varIntBytes);
        prefixedBuf.writeBytes(msg.getBuf());

        msg.release();

        return prefixedBuf;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf out) {
        ByteMessage msg = new ByteMessage(out);
        msg.writeVarInt(buf.readableBytes());
        msg.writeBytes(buf);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
        int anticipatedRequiredCapacity = 5 + msg.readableBytes();
        return ctx.alloc().heapBuffer(anticipatedRequiredCapacity);
    }
}
