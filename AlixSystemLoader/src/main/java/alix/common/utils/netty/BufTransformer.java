package alix.common.utils.netty;

import io.netty.buffer.ByteBuf;

import java.util.function.Function;

public interface BufTransformer extends Function<ByteBuf, ByteBuf> {

    BufTransformer CONST = BufUtils::constBuffer;

}