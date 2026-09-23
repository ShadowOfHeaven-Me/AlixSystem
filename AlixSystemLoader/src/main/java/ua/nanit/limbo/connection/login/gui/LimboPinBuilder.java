package ua.nanit.limbo.connection.login.gui;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.utils.config.ConfigProvider;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryItems;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryOpen;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.*;
import static ua.nanit.limbo.connection.login.packets.SoundPackets.*;

public final class LimboPinBuilder implements LimboGUI {

    //Was a hardcoded English literal ("&cPin invalid length"), bypassing the Messages/i18n system that every
    //other message/item in this class already goes through (see PIN_CONFIRM_ITEM etc. just below).
    private static final PacketSnapshot pinInvalidLengthMessagePacket = PacketPlayOutMessage.snapshot(Messages.get("pin-invalid-length"));

    private static final int[] PIN_DIGIT_SLOTS = new int[]{28, 0, 1, 2, 9, 10, 11, 18, 19, 20};
    public static final int maxLoginAttempts = ConfigProvider.config.getInt("max-login-attempts");
    private static final boolean pinAutoConfirm = true;
    //private static final String pinRegister = Messages.get("pin-register");
    //private static final AlixMessage pinRegisterBottomLine = Messages.getAsObject("pin-register-bottom-line");
    private static final int[] EMPTY_DIGIT_SLOTS = new int[]{13, 14, 15, 16};
    private static final int FIRST_EMPTY_DIGIT_SLOT = EMPTY_DIGIT_SLOTS[0];
    public static final ItemStack
            PIN_CONFIRM_ITEM = of(ItemTypes.GREEN_WOOL, Messages.get("pin-confirm")),
            PIN_LAST_REMOVE_ITEM = of(ItemTypes.YELLOW_WOOL, Messages.get("pin-remove-last")),
            PIN_RESET_ITEM = of(ItemTypes.RED_WOOL, Messages.get("pin-reset")),
            PIN_LEAVE_ITEM = of(ItemTypes.BLACK_WOOL, Messages.get("pin-leave")),
            RECOVER_ITEM = of(ItemTypes.PAPER, Messages.get("gui-recover-account"));
    private static final int
            ACTION_PIN_CONFIRM = 22,
            ACTION_LAST_REMOVE = 23,
            ACTION_RESET = 24,
            ACTION_LEAVE = 25,
            ACTION_RECOVER = 26;

    private static final ItemStack[] pinVerificationGuiItems = createPINVerificationItems();

    private static final PacketSnapshot invItemsPacket = new PacketPlayOutInventoryItems(pinVerificationGuiItems).toSnapshot();
    //Were hardcoded English literals ("Register"/"Login"), same bug/fix as AnvilBuilderGoal's window titles -
    //reusing the same "gui-title-login"/"gui-title-register" keys since it's the exact same displayed text,
    //just for this PIN-based GUI instead of the anvil-based one.
    private static final PacketSnapshot
            registerInvOpen = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.GENERIC_9X4, Messages.get("gui-title-register")),
            loginInvOpen = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.GENERIC_9X4, Messages.get("gui-title-login"));


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
        if (data != null && data.canUseAnyRecovery()) {
            this.items.set(ACTION_RECOVER, RECOVER_ITEM);
            this.spoofWithSnapshot = false;
        } else {
            this.spoofWithSnapshot = true;
        }
        this.invOpenPacket = PersistentUserData.isRegistered(data) ? registerInvOpen : loginInvOpen;
    }

    private void spoofAllItems() {
        this.duplexHandler.writeAndFlush(this.spoofWithSnapshot ? invItemsPacket : new PacketPlayOutInventoryItems(this.items));
    }

    private void setItem(int i, ItemStack item) {
        this.items.set(i, item);
        this.spoofWithSnapshot = false;
    }

    private static ItemStack[] createPINVerificationItems() {
        ItemStack[] items = new ItemStack[36];
        Arrays.fill(items, BACKGROUND_ITEM);

        for (byte i = 0; i <= 9; i++) items[PIN_DIGIT_SLOTS[i]] = DIGITS[i];

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
                this.onPINConfirmation(spoofItems -> {
                    if (spoofItems) this.spoofAllItems();
                });
                return;
            } else if (pin.length() != 4)
                this.duplexHandler.writeAndFlush(NOTE_BLOCK_HARP);//adding a pin digit

        } else if (performAction(slot)) {
            this.onPINConfirmation(spoofItems -> {
                if (spoofItems) this.spoofAllItems();
            });
            return;
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

    private void onPINConfirmation(Consumer<Boolean> callback) {
        String pin = this.getPasswordBuilt();

        if (PersistentUserData.isRegistered(data)) {
            if (this.loginState.isPasswordCorrect(pin)) {
                this.duplexHandler.writeAndFlush(PLAYER_LEVELUP);
                this.loginState.tryLogIn();
                callback.accept(false);
                return;
            }

            if (this.loginState.onIncorrectPassword()) {
                this.resetPin0();
                callback.accept(true);
                return;
            }
            callback.accept(false);
            return;
        }
        this.duplexHandler.writeAndFlush(PLAYER_LEVELUP);
        //PIN logins never trigger the (network-bound) HaveIBeenPwned check - see
        //AlixCommonUtils#getPasswordInvalidityReasonAsync - so this callback always fires synchronously here.
        this.loginState.registerIfValid(pin, LoginType.PIN, data -> callback.accept(data == null));
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

        if (slot == ACTION_RECOVER && this.data != null && this.data.canUseAnyRecovery()) {
            this.loginState.openRecovery();
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
