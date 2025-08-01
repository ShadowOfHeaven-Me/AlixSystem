package alix.velocity.systems.packets.gui.impl;

import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;

import java.util.Arrays;


public abstract class AccountGUI extends AlixGUI {

    private AccountGUI(InventoryGui inv) {
        super(inv);
    }

    public static void add(VerifiedUser user) {
        new AccountGUIModern(new InventoryGui(user, AlixInventoryType.GENERIC_3X3, Messages.get("gui-title-account"))).map();
    }

    private static final class AccountGUIModern extends AccountGUI {

        private AccountGUIModern(InventoryGui inv) {
            super(inv);
        }

        @Override
        protected GUIItem[] create(InventoryGui unusedNull) {
            GUIItem[] items = new GUIItem[9];
            Arrays.fill(items, BACKGROUND_ITEM);

            ItemStack i1 = create(ItemTypes.IRON_BARS, Messages.get("gui-account-passwords"));
            items[0] = new GUIItem(i1, inv -> PasswordsGUI.add(this.user, this));

            ItemStack i2 = create(ItemTypes.NETHER_STAR, Messages.get("gui-account-login-settings"));
            items[1] = new GUIItem(i2, inv -> LoginSettingsGUI.add(this.user, this));

            //ItemStack i3 = LimboAuthBuilder.ofSkull(Messages.get("gui-account-google-authenticator"), SkullTextures.GOOGLE_AUTH);
            //items[2] = new GUIItem(i3, inv -> GoogleAuthGUI.add(this.user));

            return items;
        }
    }
}