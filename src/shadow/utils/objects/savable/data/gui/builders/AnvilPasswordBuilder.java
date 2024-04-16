package shadow.utils.objects.savable.data.gui.builders;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.utils.holders.packet.buffered.PacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.NettyUtils;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.users.types.VerifiedUser;

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
    private final IntFunction<ByteBuf> allItemsSupplier, invalidIndicateItemsSupplier;
    private final Consumer<String> onValidPasswordConfirmation;
    private final Runnable returnOriginalGui;
    private ByteBuf allItemsBuffer, invalidIndicateItemsBuffer;
    //private Object nmsItemList;
    private String password, invalidityReason;
    private boolean isPasswordValid;

    public AnvilPasswordBuilder(UnverifiedUser user) {
        this.ctx = user.silentContext();
        this.gui = user.isRegistered() ? loginGUI : registerGUI;
        this.isPasswordValid = user.isRegistered();
        this.allItemsSupplier = PacketConstructor.AnvilGUI::allItems;
        this.invalidIndicateItemsSupplier = PacketConstructor.AnvilGUI::invalidIndicate;
        this.onValidPasswordConfirmation = null;//not used here
        this.returnOriginalGui = null;//not used here
        //this.isBufferConst = true;
    }

    public AnvilPasswordBuilder(VerifiedUser user, boolean pin, Consumer<String> onValidPasswordConfirmation, Runnable returnOriginalGui) {
        this.ctx = user.silentContext();
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
    public void onClick(InventoryClickEvent event) {//invoked when this gui is used to change a password
        switch (event.getRawSlot()) {
            case 1:
                this.returnOriginalGui.run();
                return;
            case 2:
                if (isPasswordValid) this.onValidPasswordConfirmation.accept(this.password);
                else {
                    Player player = (Player) event.getWhoClicked();
                    if (this.invalidityReason != null) AlixUtils.sendMessage(player, this.invalidityReason);
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

    public void updateWindowId(int windowId) {
        if (windowId == 0) return;//it's the player's own inventory
        this.allItemsBuffer = this.allItemsSupplier.apply(windowId);
        this.invalidIndicateItemsBuffer = this.invalidIndicateItemsSupplier.apply(windowId);
        this.spoofValidAccordingly();
    }

    public void spoofAllItems() {
        this.spoof0(allItemsBuffer);
    }

    public void spoofValidAccordingly() {
        this.spoof0(isPasswordValid ? allItemsBuffer : invalidIndicateItemsBuffer);
    }

    public void spoofItemsInvalidIndicate() {
        this.spoof0(invalidIndicateItemsBuffer);
    }

    private void spoof0(ByteBuf packet) {
        NettyUtils.writeAndFlushConst(this.ctx, packet);
        //this.ctx.writeAndFlush(packet);
        //NettyUtils.writeAndFlush(this.ctx, packet);
    }

    public static void init() {
    }
}