package shadow.utils.misc.packet.constructors;

import alix.common.packets.message.MessageWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerDisconnect;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;

public final class OutDisconnectPacketConstructor {

    //Were Component.text(message) - see OutMessagePacketConstructor for why that breaks hex codes.
    public static ByteBuf constructDynamicAtLoginPhase(String message) {
        return NettyUtils.createBuffer(new WrapperLoginServerDisconnect(MessageWrapper.parseLegacy(message)), false);
    }

    public static ByteBuf constructConstAtLoginPhase(String message) {
        return NettyUtils.constBuffer(new WrapperLoginServerDisconnect(MessageWrapper.parseLegacy(message)));
    }

    public static ByteBuf dynamicAtConfig(String message) {
        return NettyUtils.createBuffer(new WrapperConfigServerDisconnect(MessageWrapper.parseLegacy(message)), false);
    }

    public static ByteBuf dynamicAtPlay(String message) {
        return NettyUtils.createBuffer(new WrapperPlayServerDisconnect(MessageWrapper.parseLegacy(message)), false);
    }

    public static ByteBuf constAtPlay(String message) {
        return NettyUtils.constBuffer(new WrapperPlayServerDisconnect(MessageWrapper.parseLegacy(message)));
    }

    private OutDisconnectPacketConstructor() {
    }
}