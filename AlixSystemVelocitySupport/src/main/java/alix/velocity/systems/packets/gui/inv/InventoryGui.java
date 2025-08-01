package alix.velocity.systems.packets.gui.inv;

import alix.common.packets.inventory.AlixInventoryType;
import alix.common.packets.inventory.InventoryWrapper;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public final class InventoryGui implements AbstractInventory {

    private final List<ItemStack> items;
    private final VerifiedUser user;
    private final WrapperPlayServerOpenWindow openWrapper;

    public InventoryGui(VerifiedUser user, AlixInventoryType type, String title) {
        this(items(type.size()), user, type, title);
    }

    public InventoryGui(List<ItemStack> items, VerifiedUser user, AlixInventoryType type, String title) {
        this.items = items;
        this.user = user;
        this.openWrapper = InventoryWrapper.createInvOpen(type, Component.text(title), user.user.getClientVersion());
    }

    public static List<ItemStack> items(int len) {
        List<ItemStack> list = new ArrayList<>(len);
        while (len-- > 0) list.add(ItemStack.EMPTY);
        return list;
    }

    @Override
    public VerifiedUser getUser() {
        return user;
    }

    @Override
    public void setItem(int i, ItemStack item) {
        this.items.set(i, item);
    }

    @Override
    public void open() {
        this.user.writePacketSilently(this.openWrapper);
        this.spoofItems();
    }

    @Override
    public void spoofItems() {
        this.user.sendPacketSilently(this.createItemsWrapper());
    }

    private WrapperPlayServerWindowItems createItemsWrapper() {
        return new WrapperPlayServerWindowItems(1, 1, this.items, null);
    }

    @Override
    public int getSize() {
        return this.items.size();
    }
}