package alix.common.packets.inventory;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import net.kyori.adventure.text.Component;

public final class InventoryWrapper {

    public static WrapperPlayServerOpenWindow createInvOpen(AlixInventoryType type, Component title, ClientVersion version) {
        WrapperPlayServerOpenWindow wrapper;

        //window id = 1

        wrapper = new WrapperPlayServerOpenWindow(1, type.getId(version), title);

        String oldType = type == AlixInventoryType.ANVIL ? "minecraft:anvil" : "minecraft:container";
        wrapper.setLegacyType(oldType);

        return wrapper;
    }

}