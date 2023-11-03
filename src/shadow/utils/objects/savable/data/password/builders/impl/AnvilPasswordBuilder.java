package shadow.utils.objects.savable.data.password.builders.impl;

import alix.common.scheduler.impl.AlixScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import shadow.utils.holders.packet.buffered.BufferedPackets;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.objects.savable.data.password.builders.BuilderType;
import shadow.utils.objects.savable.data.password.builders.PasswordBuilder;
import shadow.utils.objects.savable.data.password.builders.PasswordGui;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class AnvilPasswordBuilder implements PasswordBuilder {

    private static final ItemStack USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON, INVALID_PASSWORD;
    private static final Object ALL_ITEMS_LIST, INVALID_INDICATE_ITEMS_LIST;

    static {
        USER_INPUT = new ItemStack(Material.PAPER);
        LEAVE_BUTTON = new ItemStack(Material.BLACK_WOOL);
        CONFIRM_BUTTON = new ItemStack(Material.LIME_WOOL);
        INVALID_PASSWORD = new ItemStack(Material.RED_WOOL);
        rename(USER_INPUT, "§0");
        rename(LEAVE_BUTTON, PasswordGui.pinLeave);
        rename(CONFIRM_BUTTON, PasswordGui.pinConfirm);
        rename(INVALID_PASSWORD, PasswordGui.invalidPassword);
        ALL_ITEMS_LIST = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON));
        INVALID_INDICATE_ITEMS_LIST = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, INVALID_PASSWORD));
    }

    private final Inventory gui;
    private final UnverifiedUser user;
    private Object allItemsPacket, invalidIndicateItemsPacket;
    //private Object nmsItemList;
    private String password = "";
    private int windowId;
    private boolean isPasswordValid;

    public AnvilPasswordBuilder(UnverifiedUser user) {
        this.user = user;
        this.gui = Bukkit.createInventory(user.getPlayer(), InventoryType.ANVIL, user.isRegistered() ? PasswordGui.guiTitleLogin : PasswordGui.guiTitleRegister);
        this.isPasswordValid = user.isRegistered();
    }

    @NotNull
    @Override
    public Inventory getGUI() {
        return gui;
    }

    public void input(String text, boolean valid) {
        this.password = text;
        this.isPasswordValid = valid;
    }

    @Override
    public BuilderType getType() {
        return BuilderType.ANVIL;
    }

    @NotNull
    @Override
    public String getPasswordBuilt() {
        return password;
    }

    public void updateWindowID() {
        this.windowId++;
        this.allItemsPacket = BufferedPackets.getAllItemsPacketOf(windowId);
        if (allItemsPacket == null) {
            AlixScheduler.sync(() -> user.getPlayer().kickPlayer("§cSomething went wrong - " + windowId));
            return;
        }
        //if (!user.isRegistered()) {
        this.invalidIndicateItemsPacket = BufferedPackets.getInvalidIndicatePacketOf(windowId);
        if (invalidIndicateItemsPacket == null) {
            AlixScheduler.sync(() -> user.getPlayer().kickPlayer("§cSomething went wrong - " + windowId));
        }
        //}
    }

    public void spoofAllItems() {
        this.spoof(allItemsPacket);
    }

    public void spoofValidAccordingly() {
        this.spoof(isPasswordValid ? allItemsPacket : invalidIndicateItemsPacket);
    }

    public void spoofItemsInvalidIndicate() {
        this.spoof(invalidIndicateItemsPacket);
    }

    private void spoof(Object packet) {
        if (packet == null)
            AlixScheduler.sync(() -> user.getPlayer().kickPlayer("§cSomething went wrong - Nullability & " + windowId));
        AlixScheduler.runLaterAsync(() -> user.getPacketBlocker().getChannel().writeAndFlush(packet), 50, TimeUnit.MILLISECONDS);
    }

    private static void rename(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public static void init() {
        BufferedPackets.init(ALL_ITEMS_LIST, INVALID_INDICATE_ITEMS_LIST);
    }
}