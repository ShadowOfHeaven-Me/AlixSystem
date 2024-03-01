package shadow.utils.objects.savable.data.gui.builders;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.utils.netty.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.buffered.PacketConstructor;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.User;
import shadow.utils.users.offline.UnverifiedUser;

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
    private final ChannelHandlerContext ctx;
    private final IntFunction<Object> allItemsSupplier, invalidIndicateItemsSupplier;
    private final Consumer<String> onValidPasswordConfirmation;
    private final Runnable returnOriginalGui;
    private Object allItemsPacket, invalidIndicateItemsPacket;
    //private Object nmsItemList;
    private String password = "", invalidityReason;
    private int windowId;
    private boolean isPasswordValid;

    public AnvilPasswordBuilder(UnverifiedUser user) {
        this.ctx = user.getDuplexHandler().getSilentContext();
        this.gui = user.isRegistered() ? loginGUI : registerGUI;
        this.isPasswordValid = user.isRegistered();
        this.allItemsSupplier = PacketConstructor.AnvilGUI::allItems;
        this.invalidIndicateItemsSupplier = PacketConstructor.AnvilGUI::invalidIndicate;
        this.onValidPasswordConfirmation = null;//not used here
        this.returnOriginalGui = null;//not used here
    }

    public AnvilPasswordBuilder(User user, boolean pin, Consumer<String> onValidPasswordConfirmation, Runnable returnOriginalGui) {
        this.ctx = user.getDuplexHandler().getSilentContext();
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

/*    public void updateWindowID() {
        this.updateWindowID(windowId + 1);
    }*/

    public void onOutWindowOpenPacket(Object packet) throws Exception {
        this.windowId = (int) ReflectionUtils.outWindowOpenIdMethod.invoke(packet);
        this.allItemsPacket = allItemsSupplier.apply(windowId);
        this.invalidIndicateItemsPacket = invalidIndicateItemsSupplier.apply(windowId);
        this.spoofValidAccordingly();
    }

    public void spoofAllItems() {
        this.spoof0(allItemsPacket);
    }

    public void spoofValidAccordingly() {
        this.spoof0(isPasswordValid ? allItemsPacket : invalidIndicateItemsPacket);
    }

    public void spoofItemsInvalidIndicate() {
        this.spoof0(invalidIndicateItemsPacket);
    }

    private void spoof0(Object packet) {
        NettyUtils.writeAndFlush(this.ctx, packet);
        //this.ctx.writeAndFlush(packet);
    }

    public static void init() {
    }
}