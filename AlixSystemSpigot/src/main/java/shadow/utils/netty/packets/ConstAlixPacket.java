package shadow.utils.netty.packets;

import alix.common.utils.netty.WrapperTransformer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import shadow.utils.users.types.AlixUser;

final class ConstAlixPacket extends AbstractAlixPacket {

    ConstAlixPacket(PacketWrapper<?>[] wrappers) {
        super(AlixPacket.constructBufs(wrappers, WrapperTransformer.CONST));
    }

    ConstAlixPacket(ByteBuf[] bufs) {
        super(bufs);
    }

    @Override
    public void write(AlixUser user) {
        this.forEach(user::writeConstSilently);
    }

    @Override
    public boolean isConst() {
        return true;
    }
}