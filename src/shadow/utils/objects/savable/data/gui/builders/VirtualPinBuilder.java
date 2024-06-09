package shadow.utils.objects.savable.data.gui.builders;

import alix.common.data.LoginType;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.systems.commands.CommandManager;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.objects.savable.data.gui.AlixVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.objects.savable.data.gui.virtual.CachingVirtualInventory;
import shadow.utils.objects.savable.data.gui.virtual.VirtualInventory;
import shadow.utils.users.types.UnverifiedUser;

import java.util.ArrayList;

import static shadow.utils.main.AlixUtils.maxLoginAttempts;
import static shadow.utils.objects.savable.data.gui.PasswordGui.*;

public final class VirtualPinBuilder implements AlixVerificationGui {

    private static final boolean pinAutoConfirm = Main.config.getBoolean("pin-auto-confirm");
    private static final String pinRegister = Messages.get("pin-register");
    private static final AlixMessage pinRegisterBottomLine = Messages.getAsObject("pin-register-bottom-line");

    private static final ByteBuf invItemsByteBuf = CachingVirtualInventory.constInvItemsByteBuf(PasswordGui.retrooperPinVerificationGuiItems);
    private static final ByteBuf pinInvalidLengthMessagePacket = OutMessagePacketConstructor.constructConst(pinInvalidLength);
    private static final ByteBuf
            registerInvOpenBuffer = CachingVirtualInventory.constInvOpenByteBuf(36, guiTitleRegister),
            loginInvOpenBuffer = CachingVirtualInventory.constInvOpenByteBuf(36, guiTitleLogin);

    private StringBuilder pin = new StringBuilder(4);
    private final CachingVirtualInventory gui;
    private final UnverifiedUser user;

    public VirtualPinBuilder(UnverifiedUser user) {
        this.user = user;
        this.gui = new CachingVirtualInventory(
                this.user.silentContext(),
                new ArrayList<>(PasswordGui.retrooperPinVerificationGuiItems),
                invItemsByteBuf,
                user.isRegistered() ? loginInvOpenBuffer : registerInvOpenBuffer);
        this.gui.setSpoofWithCached(true);
    }

    @Override
    @NotNull
    public VirtualInventory getVirtualGUI() {
        return this.gui;
    }

    @Override
    public void select(int slot) {//returns whether the login action should be performed
        byte digit = getDigit(slot);
        if (digit != -1) {
            boolean login = append(digit);

            if (login && pinAutoConfirm) {//logging in
                this.onPINConfirmation();
            } else if (pin.length() != 4) this.user.writeAndFlushConstSilently(noteBlockHarpSoundPacket);//adding a pin digit
            return;
        }
        if (performAction(slot)) this.onPINConfirmation();
    }

    private void onPINConfirmation() {
        String pin = this.getPasswordBuilt();

        if (user.isRegistered()) {
            if (user.isPasswordCorrect(pin)) {
                this.user.writeAndFlushConstSilently(playerLevelUpSoundPacket);
                this.user.logIn();
                return;
            } else if (++user.loginAttempts == maxLoginAttempts)
                MethodProvider.kickAsync(user, CommandManager.incorrectPasswordKickPacket);
            else {//beautiful syntax
                this.resetPin0();
                this.user.writeAndFlushConstSilently(CommandManager.incorrectPasswordMessagePacket);
            }
            return;
        }
        this.user.writeAndFlushConstSilently(playerLevelUpSoundPacket);
        this.user.registerAsync(pin);
        this.user.getPlayer().sendTitle(pinRegister, pinRegisterBottomLine.format(pin), 0, 100, 50);
    }

    @Override
    public LoginType getType() {
        return LoginType.PIN;
    }

    private void resetPin0() {
        if (pin.length() != 0) {
            this.pin = new StringBuilder(4);
            for (int i : EMPTY_DIGIT_SLOTS)
                gui.setItem(i, RETROOPER_BARRIER);
            this.user.writeAndFlushConstSilently(itemBreakSoundPacket);
        }
        this.gui.setSpoofWithCached(true);
    }

    private boolean performAction(int slot) {
        if (slot == ACTION_PIN_CONFIRM) {
            if (this.pin.length() != 4) {
                this.user.writeConstSilently(pinInvalidLengthMessagePacket);
                this.user.writeAndFlushConstSilently(villagerNoSoundPacket);
                return false;
            } else {
                this.user.writeAndFlushConstSilently(playerLevelUpSoundPacket);
                return true;
            }
        }

        if (slot == ACTION_LAST_REMOVE) {
            if (this.pin.length() != 0) {
                this.pin.deleteCharAt(pin.length() - 1);
                this.gui.setItem(FIRST_EMPTY_DIGIT_SLOT + pin.length(), RETROOPER_BARRIER);
                this.user.writeAndFlushConstSilently(noteBlockSnareSoundPacket);
            }
            if (this.pin.length() == 0) this.gui.setSpoofWithCached(true);
            return false;
        }

        if (slot == ACTION_LEAVE) {
            MethodProvider.kickAsync(user, pinLeaveFeedbackKickPacket);
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
        gui.setItem(FIRST_EMPTY_DIGIT_SLOT + pin.length(), retrooperDigits[digit]);
        pin.append(digit);

        if (!pinAutoConfirm && pin.length() == 4)
            this.user.writeAndFlushConstSilently(noteBlockHarpSoundPacket);

        return pinAutoConfirm && pin.length() == 4;
    }

    @Override
    public String getPasswordBuilt() {
        return pin.toString();
    }
}
