package alix.common.utils.netty;

import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;

import java.util.function.Function;

public interface WrapperTransformer extends Function<PacketWrapper<?>, ByteBuf> {

    WrapperTransformer CONST = BufUtils::constBuffer;
    WrapperTransformer DYNAMIC = BufUtils::createBuffer;
    WrapperTransformer DYNAMIC_NO_ID = BufUtils::createBufferNoID;

}