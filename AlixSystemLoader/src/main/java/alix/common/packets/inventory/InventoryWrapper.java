package alix.common.packets.inventory;

import alix.common.utils.netty.WrapperUtils;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import net.kyori.adventure.text.Component;

public final class InventoryWrapper {

    public static WrapperPlayServerOpenWindow createInvOpen(AlixInventoryType type, Component title, ClientVersion version) {
        WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(1, type.getId(version), title);

        WrapperUtils.setVersion(wrapper, version.toServerVersion());

        String oldType = type == AlixInventoryType.ANVIL ? "minecraft:anvil" : "minecraft:container";
        wrapper.setLegacyType(oldType);

        return wrapper;
    }

}