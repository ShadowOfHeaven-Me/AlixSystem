package shadow.utils.misc.packet.buffered;

import io.netty.buffer.ByteBuf;

public interface AnvilPacketSupplier {

    ByteBuf allItemsBuffer();

    ByteBuf invalidIndicateBuffer();

    //returns true if should spoof accordingly, false if no spoof should be performed
    boolean onInput(String input);

    String getInput();

    void onWindowUpdate(int id);

}