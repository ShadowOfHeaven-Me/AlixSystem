package ua.nanit.limbo.connection.login.gui;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryItems;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryOpen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.nanit.limbo.connection.login.packets.SoundPackets.*;
import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.*;

public final class LimboPinBuilder implements LimboGUI {

    public static final PacketSnapshot incorrectPasswordKickPacket
            = PacketPlayOutDisconnect.snapshot("&cIncorrect password");

    private static final PacketSnapshot
            incorrectPasswordMessagePacket = PacketPlayOutMessage.snapshot("&cIncorrect password"),
            pinInvalidLengthMessagePacket = PacketPlayOutMessage.snapshot("&cPin invalid length");

    private static final int[] PIN_DIGIT_SLOTS = new int[]{28, 0, 1, 2, 9, 10, 11, 18, 19, 20};
    public static final int maxLoginAttempts = 2;
    private static final boolean pinAutoConfirm = true;
    //private static final String pinRegister = Messages.get("pin-register");
    //private static final AlixMessage pinRegisterBottomLine = Messages.getAsObject("pin-register-bottom-line");
    private static final int[] EMPTY_DIGIT_SLOTS = new int[]{13, 14, 15, 16};
    private static final int FIRST_EMPTY_DIGIT_SLOT = EMPTY_DIGIT_SLOTS[0];
    public static final ItemStack
            PIN_CONFIRM_ITEM = of(ItemTypes.GREEN_WOOL, Messages.get("pin-confirm")),
            PIN_LAST_REMOVE_ITEM = of(ItemTypes.YELLOW_WOOL, Messages.get("pin-remove-last")),
            PIN_RESET_ITEM = of(ItemTypes.RED_WOOL, Messages.get("pin-reset")),
            PIN_LEAVE_ITEM = of(ItemTypes.BLACK_WOOL, Messages.get("pin-leave"));
    private static final int
            ACTION_PIN_CONFIRM = 22,
            ACTION_LAST_REMOVE = 23,
            ACTION_RESET = 24,
            ACTION_LEAVE = 25;

    private static final ItemStack[] pinVerificationGuiItems = createPINVerificationItems();

    private static final PacketSnapshot invItemsPacket = new PacketPlayOutInventoryItems(pinVerificationGuiItems).toSnapshot();
    private static final PacketSnapshot
            registerInvOpen = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.GENERIC_9X4, "Register"),
            loginInvOpen = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.GENERIC_9X4, "Login");


    private final StringBuilder pin = new StringBuilder(4);
    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private final PersistentUserData data;
    private final LoginState loginState;
    private final List<ItemStack> items;
    private final PacketSnapshot invOpenPacket;

    private boolean spoofWithSnapshot;
    //private final PacketSnapshot cachedItems;

    public LimboPinBuilder(ClientConnection connection, PersistentUserData data, LoginState loginState) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        this.data = data;
        this.loginState = loginState;
        this.items = new ArrayList<>(Arrays.asList(pinVerificationGuiItems));
        this.invOpenPacket = PersistentUserData.isRegistered(data) ? registerInvOpen : loginInvOpen;
        this.spoofWithSnapshot = true;
    }

    private void spoofAllItems() {
        if (this.spoofWithSnapshot) this.duplexHandler.writeAndFlush(invItemsPacket);
        else this.duplexHandler.writeAndFlush(new PacketPlayOutInventoryItems(this.items));
    }

    private void setItem(int i, ItemStack item) {
        this.items.set(i, item);
        this.spoofWithSnapshot = false;
    }

    private static ItemStack[] createPINVerificationItems() {
        //Inventory inv = createNew(null);//we do not care about the title
        ItemStack[] items = new ItemStack[36];
        Arrays.fill(items, BACKGROUND_ITEM);

/*        for (byte i = 0; i < 36; i++) {
            ItemStack item = items[i];
            if (item == null || ItemTypes.isAir(item.getType())) items[i] = BACKGROUND_ITEM;
        }*/

        for (byte i = 0; i <= 9; i++) items[PIN_DIGIT_SLOTS[i]] = DIGITS[i];

/*        items[DIGIT_0, digits[0]);
        items[DIGIT_1, digits[1]);
        items[DIGIT_2, digits[2]);
        items[DIGIT_3, digits[3]);
        items[DIGIT_4, digits[4]);
        items[DIGIT_5, digits[5]);
        items[DIGIT_6, digits[6]);
        items[DIGIT_7, digits[7]);
        items[DIGIT_8, digits[8]);
        items[DIGIT_9, digits[9]);*/

        items[ACTION_PIN_CONFIRM] = PIN_CONFIRM_ITEM;
        items[ACTION_LAST_REMOVE] = PIN_LAST_REMOVE_ITEM;
        items[ACTION_RESET] = PIN_RESET_ITEM;
        items[ACTION_LEAVE] = PIN_LEAVE_ITEM;

        for (int i : EMPTY_DIGIT_SLOTS) items[i] = BARRIER;
        return items;
    }

    @Override
    public void select(int slot) {
        byte digit = getDigit(slot);
        if (digit != -1) {
            boolean login = append(digit);

            if (login && pinAutoConfirm) {//logging in
                boolean spoofItems = this.onPINConfirmation();
                if (!spoofItems) return;
            } else if (pin.length() != 4)
                this.duplexHandler.writeAndFlush(NOTE_BLOCK_HARP);//adding a pin digit

        } else if (performAction(slot)) {
            boolean spoofItems = this.onPINConfirmation();
            if (!spoofItems) return;
        }
        this.spoofAllItems();
    }

    @Override
    public void onCloseAttempt() {
        this.show();
    }

    @Override
    public void show() {
        this.duplexHandler.write(this.invOpenPacket);
        this.spoofAllItems();
    }

    private boolean onPINConfirmation() {
        String pin = this.getPasswordBuilt();

        if (PersistentUserData.isRegistered(data)) {
            if (this.loginState.isPasswordCorrect(pin)) {
                this.duplexHandler.writeAndFlush(PLAYER_LEVELUP);
                this.loginState.tryLogIn();
                return false;
            }

            if (this.loginState.onIncorrectPassword()) {
                this.resetPin0();
                return true;
            }
            return false;
        }
        this.duplexHandler.writeAndFlush(PLAYER_LEVELUP);
        return this.loginState.registerIfValid(pin, LoginType.PIN) == null;
        //this.connection.getPlayer().sendTitle(pinRegister, pinRegisterBottomLine.format(pin), 0, 100, 50);
    }

    private void resetPin0() {
        if (pin.length() != 0) {
            this.pin.setLength(0);
            for (int i : EMPTY_DIGIT_SLOTS)
                this.setItem(i, BARRIER);
            this.duplexHandler.writeAndFlush(ITEM_BREAK);
        }
        this.spoofWithSnapshot = true;
    }

    private boolean performAction(int slot) {
        if (slot == ACTION_PIN_CONFIRM) {
            if (this.pin.length() != 4) {
                this.duplexHandler.write(pinInvalidLengthMessagePacket);
                this.duplexHandler.writeAndFlush(VILLAGER_NO);
                return false;
            } else {
                this.duplexHandler.writeAndFlush(PLAYER_LEVELUP);
                return true;
            }
        }

        if (slot == ACTION_LAST_REMOVE) {
            if (this.pin.length() != 0) {
                this.pin.deleteCharAt(pin.length() - 1);
                this.setItem(FIRST_EMPTY_DIGIT_SLOT + pin.length(), BARRIER);
                this.duplexHandler.writeAndFlush(NOTE_BLOCK_SNARE);
            }
            if (this.pin.length() == 0) this.spoofWithSnapshot = true;
            return false;
        }

        if (slot == ACTION_LEAVE) {
            this.connection.sendPacketAndClose(leaveFeedbackKickPacket);
            return false;
        }

        if (slot == ACTION_RESET) {
            this.resetPin0();
            return false;
        }

        return false;
    }

    private static byte getDigit(int slot) {
        for (byte i = 0; i <= 9; i++) if (PIN_DIGIT_SLOTS[i] == slot) return i;
        return -1;
    }

    private boolean append(byte digit) {
        if (pin.length() == 4) return false;
        this.setItem(FIRST_EMPTY_DIGIT_SLOT + pin.length(), DIGITS[digit]);
        pin.append(digit);

        if (!pinAutoConfirm && pin.length() == 4)
            this.duplexHandler.writeAndFlush(NOTE_BLOCK_HARP);

        return pinAutoConfirm && pin.length() == 4;
    }

    public String getPasswordBuilt() {
        return pin.toString();
    }
}
