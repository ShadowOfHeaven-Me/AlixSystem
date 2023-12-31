package shadow.utils.objects.savable.data.gui.builders;

import shadow.utils.objects.savable.data.gui.AlixGui;
import alix.common.data.GuiType;
import alix.common.scheduler.AlixScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import shadow.utils.holders.packet.buffered.BufferedPackets;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class AnvilPasswordBuilder implements AlixGui {

    //private static final ItemStack USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON, INVALID_PASSWORD;
    //private static final Object ALL_ITEMS_LIST, INVALID_INDICATE_ITEMS_LIST;

    static {
        ItemStack USER_INPUT = new ItemStack(Material.PAPER);
        ItemStack LEAVE_BUTTON = new ItemStack(Material.BLACK_WOOL);
        ItemStack CONFIRM_BUTTON = new ItemStack(Material.LIME_WOOL);
        ItemStack INVALID_PASSWORD = new ItemStack(Material.RED_WOOL);

        rename(USER_INPUT, "§0");
        rename(LEAVE_BUTTON, PasswordGui.pinLeave);
        rename(CONFIRM_BUTTON, PasswordGui.pinConfirm);
        rename(INVALID_PASSWORD, PasswordGui.invalidPassword);



        Object ALL_ITEMS_LIST = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON));
        Object INVALID_INDICATE_ITEMS_LIST = OutWindowItemsPacketConstructor.createNMSItemList(Arrays.asList(USER_INPUT, LEAVE_BUTTON, INVALID_PASSWORD));

        BufferedPackets.init(ALL_ITEMS_LIST, INVALID_INDICATE_ITEMS_LIST);
    }

    private static final Inventory registerGUI = Bukkit.createInventory(null, InventoryType.ANVIL, PasswordGui.guiTitleRegister),
            loginGUI = Bukkit.createInventory(null, InventoryType.ANVIL, PasswordGui.guiTitleLogin);

    private final Inventory gui;
    private final UnverifiedUser user;
    private Object allItemsPacket, invalidIndicateItemsPacket;
    //private Object nmsItemList;
    private String password = "";
    private int windowId;
    private boolean isPasswordValid;

    public AnvilPasswordBuilder(UnverifiedUser user) {
        this.user = user;
        this.gui = user.isRegistered() ? loginGUI : registerGUI;
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
    public GuiType getType() {
        return GuiType.ANVIL;
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

    }
}