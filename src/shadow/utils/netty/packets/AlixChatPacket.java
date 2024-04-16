package shadow.utils.netty.packets;

import net.kyori.adventure.text.Component;

public final class AlixChatPacket {

    private final Component component;
    private final boolean isDisguised;

    public AlixChatPacket(Component component, boolean isDisguised) {
        this.component = component;
        this.isDisguised = isDisguised;
    }

    /*public ByteBuf createByteBuf() {

    }*/
}