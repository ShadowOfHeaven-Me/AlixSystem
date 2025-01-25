package shadow.utils.objects.savable.data.gui.builders;

import alix.common.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.buffered.AnvilPacketSuppliers;
import shadow.utils.objects.savable.data.gui.bases.AnvilBuilderBase;
import shadow.utils.users.types.VerifiedUser;

import java.util.function.Consumer;

public final class BukkitAnvilPasswordBuilder extends AnvilBuilderBase implements AbstractAlixGUI {

    //private static final ItemStack USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON, INVALID_PASSWORD;
    //private static final Object ALL_ITEMS_LIST, INVALID_INDICATE_ITEMS_LIST;

    private static final Inventory
            pinChangeGUI = Bukkit.createInventory(null, InventoryType.ANVIL, Messages.get("gui-title-password-changing-pin")),
            passwordChangeGUI = Bukkit.createInventory(null, InventoryType.ANVIL, Messages.get("gui-title-password-changing"));

    private final Inventory gui;
    private final Consumer<String> onValidPasswordConfirmation;
    private final Runnable returnOriginalGui;

    public BukkitAnvilPasswordBuilder(VerifiedUser user, boolean pin, Consumer<String> onValidPasswordConfirmation, Runnable returnOriginalGui) {
        super(user.silentContext(), false, AnvilPacketSuppliers.newVerifiedSupplier());
        this.gui = pin ? pinChangeGUI : passwordChangeGUI;
        this.onValidPasswordConfirmation = onValidPasswordConfirmation;
        this.returnOriginalGui = returnOriginalGui;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
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
    public Inventory getGUI() {
        return gui;
    }

    public static void init() {
    }
}