package shadow.systems.virtualization.packets.login_impl;

import io.netty.buffer.ByteBuf;
import shadow.systems.virtualization.packets.login_impl.Ver1_20_1Impl;

import java.util.function.Supplier;

public final class PlayLoginPacket {

    private static final Supplier<ByteBuf> SUPPLIER = new Ver1_20_1Impl();

}