package ua.nanit.limbo.protocol.packets.play;

import alix.common.packets.message.MessageWrapper;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.netty.WrapperUtils;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketPlayOutMessage implements PacketOut {

    private Component component;

    public PacketPlayOutMessage() {
    }

    public static PacketSnapshot snapshot(String message) {
        return withMessage(message).toSnapshot();
    }

    /**
     * Parses the legacy '&amp;'/'§'-coded string into a Component once, immediately - the resulting
     * Component is reused as-is for every client version's encode() call (see {@link
     * MessageWrapper#parseLegacy(String)}), instead of re-parsing the same string on every call the way
     * keeping a separate String field around would require.
     */
    public static PacketPlayOutMessage withMessage(String message) {
        return withComponent(MessageWrapper.parseLegacy(AlixFormatter.translateColors(message)));
    }

    /**
     * Like {@link #withMessage(String)}, but sends a pre-built Adventure Component directly, for anything
     * the legacy string format can't express on its own (e.g. a click/hover event on part of the message).
     */
    public static PacketPlayOutMessage withComponent(Component component) {
        return new PacketPlayOutMessage().setComponent(component);
    }

    public PacketPlayOutMessage setComponent(Component component) {
        this.component = component;
        return this;
    }

    @Override
    public PacketWrapper<?> packetWrapper(Version version) {
        return MessageWrapper.createWrapper(Component.empty(), false, version.getRetrooperVersion());
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        var retrooperVersion = version.getRetrooperVersion();
        var wrapper = MessageWrapper.createWrapper(this.component, false, retrooperVersion);
        WrapperUtils.writeNoID(wrapper, msg.getBuf(), retrooperVersion);
    }
}
