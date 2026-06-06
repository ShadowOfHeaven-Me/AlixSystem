package shadow.utils.misc.packet.constructors;

import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerDisconnect;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import shadow.utils.netty.NettyUtils;

public final class OutDisconnectPacketConstructor {

    public static ByteBuf constructDynamicAtLoginPhase(String message) {
        return NettyUtils.createBuffer(new WrapperLoginServerDisconnect(Component.text(message)), false);
    }

    public static ByteBuf constructConstAtLoginPhase(String message) {
        return NettyUtils.constBuffer(new WrapperLoginServerDisconnect(Component.text(message)));
    }

    public static ByteBuf dynamicAtConfig(String message) {
        return NettyUtils.createBuffer(new WrapperConfigServerDisconnect(Component.text(message)), false);
    }

    public static ByteBuf dynamicAtPlay(String message) {
        return NettyUtils.createBuffer(new WrapperPlayServerDisconnect(Component.text(message)), false);
    }

    public static ByteBuf constAtPlay(String message) {
        return NettyUtils.constBuffer(new WrapperPlayServerDisconnect(Component.text(message)));
    }

    private OutDisconnectPacketConstructor() {
    }
}