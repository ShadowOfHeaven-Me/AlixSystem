package shadow.utils.objects.savable.data.gui.builders;

import alix.common.data.GuiType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.objects.savable.data.gui.bases.PasswordBuilderBase;
import shadow.utils.users.offline.UnverifiedUser;

import static shadow.utils.objects.savable.data.gui.PasswordGui.*;

public final class SimplePinBuilder extends PasswordBuilderBase {

    private static final boolean pinAutoConfirm = Main.config.getBoolean("pin-auto-confirm");
    private StringBuilder pin = new StringBuilder(4);
    private final Inventory gui;
    private final Location currentLocation;
    private final Player player;

    public SimplePinBuilder(UnverifiedUser user) {
        super(user);
        this.currentLocation = user.getCurrentLocation();
        this.player = user.getPlayer();
        this.gui = PasswordGui.getPINCloned();
    }

    public void select(int slot) {//returns whether the login action should be performed
        byte digit = getDigit(slot);
        if (digit != -1) {
            boolean login = append((char) (48 + digit), player);//'0' in ASCII is 48

            if (login) player.playSound(this.currentLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);//logging in
            else if (pin.length() != 4)
                player.playSound(this.currentLocation, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);//adding a pin digit

            return;
        }
        if (performAction(slot, player)) super.onPINConfirmation();
    }

    @NotNull
    @Override
    public Inventory getGUI() {
        return gui;
    }

    @Override
    public GuiType getType() {
        return GuiType.PIN;
    }

    private boolean performAction(int slot, Player player) {
        if (slot == ACTION_PIN_CONFIRM) {
            boolean complete = pin.length() == 4;
            if (!complete) {
                player.sendMessage(pinInvalidLength);
                player.playSound(this.currentLocation, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            } else {
                player.playSound(this.currentLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            return complete;
        }

        if (slot == ACTION_LAST_REMOVE) {
            if (pin.length() != 0) {
                pin.deleteCharAt(pin.length() - 1);
                gui.setItem(FIRST_EMPTY_DIGIT_SLOT + pin.length(), BARRIER);
                player.playSound(this.currentLocation, Sound.BLOCK_NOTE_BLOCK_SNARE, 1.0f, 1.0f);
            }
            return false;
        }

        if (slot == ACTION_LEAVE) {
            MethodProvider.kickAsync(user, pinLeaveFeedbackKickPacket);
            //player.kickPlayer(pinLeaveFeedback);
            return false;
        }

        if (slot == ACTION_RESET) {
            if (pin.length() != 0) {
                pin = new StringBuilder(4);
                for (int i : EMPTY_DIGIT_SLOTS) {
                    gui.setItem(i, BARRIER);
                }
                player.playSound(this.currentLocation, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
            return false;
        }

        return false;
    }

    private static byte getDigit(int slot) {
        for (byte i = 0; i < 10; i++) if (PIN_DIGIT_SLOTS[i] == slot) return i;
        return -1;
    }

/*        if (slot == DIGIT_0) return '0';
        if (slot == DIGIT_1) return '1';
        if (slot == DIGIT_2) return '2';
        if (slot == DIGIT_3) return '3';
        if (slot == DIGIT_4) return '4';
        if (slot == DIGIT_5) return '5';
        if (slot == DIGIT_6) return '6';
        if (slot == DIGIT_7) return '7';
        if (slot == DIGIT_8) return '8';
        if (slot == DIGIT_9) return '9';*/


    private boolean append(char c, Player player) {
        if (pin.length() == 4) return false;
        gui.setItem(FIRST_EMPTY_DIGIT_SLOT + pin.length(), digits[c - 48]);
        pin.append(c);

        if (!pinAutoConfirm && pin.length() == 4)
            player.playSound(this.currentLocation, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);

        return pinAutoConfirm && pin.length() == 4;
    }

    public final String getPasswordBuilt() {
        return pin.toString();
    }
}
