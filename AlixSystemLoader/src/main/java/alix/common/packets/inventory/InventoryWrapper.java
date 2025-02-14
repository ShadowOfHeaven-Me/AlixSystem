package alix.common.packets.inventory;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import net.kyori.adventure.text.Component;

import static com.github.retrooper.packetevents.protocol.player.ClientVersion.V_1_14;

public final class InventoryWrapper {

    public static WrapperPlayServerOpenWindow createInvOpen(AlixInventoryType type, Component title, ClientVersion version) {
        WrapperPlayServerOpenWindow wrapper;

        //window id = 1
        if (version.isNewerThanOrEquals(V_1_14))
            wrapper = new WrapperPlayServerOpenWindow(1, type.getId(version), title);
        else {
            String oldType = type == AlixInventoryType.ANVIL ? "minecraft:anvil" : "minecraft:container";
            wrapper = new WrapperPlayServerOpenWindow(1, oldType, title, type.size(), -1);
        }

        return wrapper;
    }

}