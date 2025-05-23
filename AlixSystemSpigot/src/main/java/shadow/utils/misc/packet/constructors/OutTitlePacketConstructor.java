package shadow.utils.misc.packet.constructors;

import alix.common.utils.netty.WrapperTransformer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleTimes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTitle;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;

public final class OutTitlePacketConstructor {

    private static final boolean modern = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17);

    //From User#sendTitle

    public static ByteBuf[] constructConst(String titleText, String subtitleText, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        return construct(titleText, subtitleText, fadeInTicks, stayTicks, fadeOutTicks, WrapperTransformer.CONST);
    }

    public static ByteBuf[] constructDynamic(String titleText, String subtitleText, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        return construct(titleText, subtitleText, fadeInTicks, stayTicks, fadeOutTicks, WrapperTransformer.DYNAMIC);
    }

    public static ByteBuf[] construct(String titleText, String subtitleText, int fadeInTicks, int stayTicks, int fadeOutTicks, WrapperTransformer transformer) {
        Component title = titleText != null ? Component.text(titleText) : null;
        Component subtitle = subtitleText != null ? Component.text(subtitleText) : null;
        PacketWrapper<?> setTitle = null;
        PacketWrapper<?> setSubtitle = null;
        PacketWrapper<?> animation;
        if (modern) {
            animation = new WrapperPlayServerSetTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);

            if (title != null) setTitle = new WrapperPlayServerSetTitleText(title);

            if (subtitle != null) setSubtitle = new WrapperPlayServerSetTitleSubtitle(subtitle);
        } else {
            animation = new WrapperPlayServerTitle(WrapperPlayServerTitle.TitleAction.SET_TIMES_AND_DISPLAY, null, null, (Component) null, fadeInTicks, stayTicks, fadeOutTicks);

            if (title != null)
                setTitle = new WrapperPlayServerTitle(WrapperPlayServerTitle.TitleAction.SET_TITLE, title, null, null, fadeInTicks, stayTicks, fadeOutTicks);

            if (subtitle != null)
                setSubtitle = new WrapperPlayServerTitle(WrapperPlayServerTitle.TitleAction.SET_SUBTITLE, null, subtitle, null, fadeInTicks, stayTicks, fadeOutTicks);
        }

        int size = 1 + (setTitle != null ? 1 : 0) + (setSubtitle != null ? 1 : 0);
        ByteBuf[] buffers = new ByteBuf[size];
        int index = 0;

        buffers[index++] = transformer.apply(animation);
        if (setTitle != null) buffers[index++] = transformer.apply(setTitle);
        if (setSubtitle != null) buffers[index] = transformer.apply(setSubtitle);

        return buffers;
    }
}