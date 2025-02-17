package ua.nanit.limbo.connection.login.gui;

import alix.common.data.PersistentUserData;
import alix.common.login.skull.SkullTextures;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.annotation.OptimizationCandidate;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.login.SoundPackets;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryItems;

import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.*;
import static ua.nanit.limbo.connection.login.gui.LimboPinBuilder.incorrectPasswordKickPacket;
import static ua.nanit.limbo.connection.login.gui.LimboPinBuilder.maxLoginAttempts;

public final class LimboAnvilBuilder implements LimboGUI {

    public static final ItemStack GO_BACK_ITEM = ofSkull("&6Go back", SkullTextures.GO_BACK);
    private static final String USER_INPUT_STR = "§f";
    private static final PacketSnapshot itemsValidWithCancelPacket, itemsValidWithLeavePacket, itemsInvalidPacket;

    static {
        ItemStack[] itemsValidWithCancel = new ItemStack[3];
        ItemStack[] itemsValidWithLeave = new ItemStack[3];
        ItemStack[] itemsInvalid = new ItemStack[3];

        ItemStack USER_INPUT = of(ItemTypes.PAPER, USER_INPUT_STR);
        ItemStack LEAVE_BUTTON = of(ItemTypes.BLACK_WOOL, "&7Leave");
        ItemStack CONFIRM_BUTTON = of(ItemTypes.LIME_WOOL, "&aConfirm");
        ItemStack INVALID_PASSWORD = of(ItemTypes.RED_WOOL, "&cInvalid password");
        ItemStack CANCEL = GO_BACK_ITEM;

        itemsValidWithLeave[0] = USER_INPUT;
        itemsValidWithLeave[1] = LEAVE_BUTTON;
        itemsValidWithLeave[2] = CONFIRM_BUTTON;

        itemsInvalid[0] = USER_INPUT;
        itemsInvalid[1] = CANCEL;
        itemsInvalid[2] = INVALID_PASSWORD;

        itemsValidWithCancel[0] = USER_INPUT;
        itemsValidWithCancel[1] = CANCEL;
        itemsValidWithCancel[2] = CONFIRM_BUTTON;

        itemsValidWithCancelPacket = new PacketPlayOutInventoryItems(itemsValidWithCancel).toSnapshot();
        itemsValidWithLeavePacket = new PacketPlayOutInventoryItems(itemsValidWithLeave).toSnapshot();
        itemsInvalidPacket = new PacketPlayOutInventoryItems(itemsInvalid).toSnapshot();
    }

    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private final PersistentUserData data;
    private final LoginState loginState;
    private final PacketSnapshot validItems, openInv;
    private final AnvilBuilderGoal goal;


    public LimboAnvilBuilder(ClientConnection connection, PersistentUserData data, LoginState loginState, AnvilBuilderGoal goal) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        this.data = data;
        this.isPasswordValid = PersistentUserData.isRegistered(data);
        this.loginState = loginState;
        this.goal = goal;
        this.validItems = goal.isUserVerified() ? itemsValidWithCancelPacket : itemsValidWithLeavePacket;
        this.openInv = goal.getInvOpen();
    }

    private String input = "";
    private String invalidityReason;
    private boolean isPasswordValid;

    public void updateText(String input) {
        this.input = input;
        this.invalidityReason = this.goal.getInvalidityReason(input);
        this.isPasswordValid = this.invalidityReason == null;
    }

    private void spoofValid() {
        this.duplexHandler.writeAndFlush(this.validItems);
    }

    private void spoofInvalid() {
        this.duplexHandler.writeAndFlush(itemsInvalidPacket);
    }

    private void spoofValidAccordingly() {
        if (this.isPasswordValid) this.spoofValid();
        else this.spoofInvalid();
    }

    private int loginAttempts;

    @Override
    public void select(int slot) {
        switch (slot) {
            case 1:
                this.connection.sendPacketAndClose(pinLeaveFeedbackKickPacket);
                break;
            case 2:
                String password = this.input;
                if (password.isEmpty()) return;
                if (PersistentUserData.isRegistered(this.data)) {
                    if (this.loginState.isPasswordCorrect(password)) {
                        this.loginState.tryLogIn();
                        return;
                    } else if (++this.loginAttempts == maxLoginAttempts)
                        this.connection.sendPacketAndClose(incorrectPasswordKickPacket);
                    return;
                }
                //can be optimized by creating PacketSnapshots for constant messages
                @OptimizationCandidate
                String reason = AlixCommonUtils.getInvalidityReason(password, false);
                if (reason != null) {
                    this.duplexHandler.write(SoundPackets.VILLAGER_NO);
                    this.duplexHandler.writeAndFlush(PacketPlayOutMessage.withMessage(reason));
                    return;
                }
                //this.user.writeConstSilently(CommandManager.passwordRegisterMessagePacket);
                //this.user.registerAsync(password);//invokes flush
                this.loginState.register(password);
        }
    }

    @Override
    public void onCloseAttempt() {
        this.show();
    }

    @Override
    public void show() {
        this.duplexHandler.write(this.openInv);
        this.spoofValidAccordingly();
    }
}