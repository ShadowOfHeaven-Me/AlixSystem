package shadow.utils.objects.savable.data.gui.builders;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.buffered.PacketConstructor;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.User;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public final class AnvilPasswordBuilder implements AlixVerificationGui, AbstractAlixGUI {

    //private static final ItemStack USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON, INVALID_PASSWORD;
    //private static final Object ALL_ITEMS_LIST, INVALID_INDICATE_ITEMS_LIST;

    private static final Inventory
            registerGUI = Bukkit.createInventory(null, InventoryType.ANVIL, PasswordGui.guiTitleRegister),
            loginGUI = Bukkit.createInventory(null, InventoryType.ANVIL, PasswordGui.guiTitleLogin),
            pinChangeGUI = Bukkit.createInventory(null, InventoryType.ANVIL, Messages.get("gui-title-password-changing-pin")),
            passwordChangeGUI = Bukkit.createInventory(null, InventoryType.ANVIL, Messages.get("gui-title-password-changing"));

    private final Inventory gui;
    //private final UnverifiedUser user;
    private final Channel channel;
    private final IntFunction<Object> allItemsSupplier, invalidIndicateItemsSupplier;
    private final Consumer<String> onValidPasswordConfirmation;
    private final Runnable returnOriginalGui;
    private Object allItemsPacket, invalidIndicateItemsPacket;
    //private Object nmsItemList;
    private String password = "", invalidityReason;
    private int windowId;
    private boolean isPasswordValid;

    public AnvilPasswordBuilder(UnverifiedUser user) {
        this.channel = user.getDuplexHandler().getChannel();
        this.gui = user.isRegistered() ? loginGUI : registerGUI;
        this.isPasswordValid = user.isRegistered();
        this.allItemsSupplier = PacketConstructor.AnvilGUI::allItems;
        this.invalidIndicateItemsSupplier = PacketConstructor.AnvilGUI::invalidIndicate;
        this.onValidPasswordConfirmation = null;//not used here
        this.returnOriginalGui = null;//not used here
    }

    public AnvilPasswordBuilder(User user, boolean pin, Consumer<String> onValidPasswordConfirmation, Runnable returnOriginalGui) {
        this.channel = user.getDuplexHandler().getChannel();
        this.gui = pin ? pinChangeGUI : passwordChangeGUI;
        this.allItemsSupplier = PacketConstructor.AnvilGUI::allItemsVerified;
        this.invalidIndicateItemsSupplier = PacketConstructor.AnvilGUI::invalidIndicateVerified;
        this.onValidPasswordConfirmation = onValidPasswordConfirmation;
        this.returnOriginalGui = returnOriginalGui;
    }

    @NotNull
    @Override
    public Inventory getGUI() {
        return gui;
    }

    public void input(String text, String invalidityReason) {
        this.password = text;
        this.isPasswordValid = invalidityReason == null;
        this.invalidityReason = invalidityReason;
    }

    @Override
    public void onClick(InventoryClickEvent event) {//invoked when this gui is used to change password
        switch (event.getRawSlot()) {
            case 1:
                this.returnOriginalGui.run();
                return;
            case 2:
                if (isPasswordValid) this.onValidPasswordConfirmation.accept(this.password);
                else {
                    Player player = (Player) event.getWhoClicked();
                    if (this.invalidityReason != null) player.sendMessage(this.invalidityReason);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
        }
    }

    @Override
    public LoginType getType() {
        return LoginType.ANVIL;
    }

    @NotNull
    @Override
    public String getPasswordBuilt() {
        return password;
    }

    private static final Object errorKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase("§cSomething went wrong");

    public void updateWindowID() {
        this.updateWindowID(windowId + 1);
    }

    public void updateWindowID(int id) {
        this.windowId = id;
        this.allItemsPacket = allItemsSupplier.apply(windowId);
        if (allItemsPacket == null) {
            MethodProvider.kickAsync(channel, errorKickPacket);
            //AlixScheduler.sync(() -> user.getPlayer().kickPlayer("§cSomething went wrong - " + windowId));
            return;
        }
        //if (!user.isRegistered()) {
        this.invalidIndicateItemsPacket = invalidIndicateItemsSupplier.apply(windowId);
        if (invalidIndicateItemsPacket == null) MethodProvider.kickAsync(channel, errorKickPacket);
        //AlixScheduler.sync(() -> user.getPlayer().kickPlayer("§cSomething went wrong - " + windowId));
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
        //if (packet == null)
        //AlixScheduler.sync(() -> user.getPlayer().kickPlayer("§cSomething went wrong - Nullability & " + windowId));
        AlixScheduler.runLaterAsync(() -> this.channel.writeAndFlush(packet), 50, TimeUnit.MILLISECONDS);
    }

    public static void init() {
    }
}