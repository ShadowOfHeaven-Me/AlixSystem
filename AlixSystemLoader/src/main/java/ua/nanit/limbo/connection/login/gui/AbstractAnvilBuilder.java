package ua.nanit.limbo.connection.login.gui;

import alix.common.login.skull.SkullTextures;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import io.netty.channel.Channel;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryItems;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.function.Consumer;

import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.of;
import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.ofSkull;

public abstract class AbstractAnvilBuilder<T extends AbstractAnvilBuilder> {

    public static final ItemStack GO_BACK_ITEM = ofSkull("&6Go back", SkullTextures.GO_BACK);
    private static final String USER_INPUT_STR = "";
    private static final PacketSnapshot itemsValidWithCancelPacket, itemsValidWithLeavePacket, itemsInvalidWithLeavePacket, itemsInvalidWithCancelPacket;

    static {
        ItemStack USER_INPUT = of(ItemTypes.PAPER, USER_INPUT_STR);
        ItemStack LEAVE_BUTTON = of(ItemTypes.BLACK_WOOL, "&7Leave");
        ItemStack CONFIRM_BUTTON = of(ItemTypes.LIME_WOOL, "&aConfirm");
        ItemStack INVALID_PASSWORD = of(ItemTypes.RED_WOOL, "&cInvalid password");
        ItemStack CANCEL = GO_BACK_ITEM;

        ItemStack[] itemsValidWithCancel = {USER_INPUT, CANCEL, CONFIRM_BUTTON};
        ItemStack[] itemsValidWithLeave = {USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON};

        ItemStack[] itemsInvalidWithCancel = {USER_INPUT, CANCEL, INVALID_PASSWORD};
        ItemStack[] itemsInvalidWithLeave = {USER_INPUT, LEAVE_BUTTON, INVALID_PASSWORD};

        itemsValidWithCancelPacket = new PacketPlayOutInventoryItems(itemsValidWithCancel).toSnapshot();
        itemsValidWithLeavePacket = new PacketPlayOutInventoryItems(itemsValidWithLeave).toSnapshot();
        itemsInvalidWithLeavePacket = new PacketPlayOutInventoryItems(itemsInvalidWithLeave).toSnapshot();
        itemsInvalidWithCancelPacket = new PacketPlayOutInventoryItems(itemsInvalidWithCancel).toSnapshot();
    }

    private final Channel channel;
    private final Version version;
    private final PacketSnapshot validItems, invalidItems, openInv;
    private final AnvilBuilderGoal goal;
    private final boolean indicateInvalid;
    private final Consumer<T> flush;

    protected AbstractAnvilBuilder(Channel channel, Version version, AnvilBuilderGoal goal, Consumer<T> flush) {
        this.channel = channel;
        this.version = version;
        this.indicateInvalid = goal.indicateInvalid();
        this.isPasswordValid = !indicateInvalid;//it's empty right now, so invalid if we validate that
        this.goal = goal;
        this.validItems = goal.isUserVerified() ? itemsValidWithCancelPacket : itemsValidWithLeavePacket;
        this.invalidItems = goal.isUserVerified() ? itemsInvalidWithCancelPacket : itemsInvalidWithLeavePacket;
        this.openInv = goal.getInvOpen();
        this.flush = flush;
    }

    protected String input = "";
    protected String invalidityReason;
    protected boolean isPasswordValid;

    private void writeAndFlush(PacketSnapshot packet) {
        this.write(packet);
        this.flush.accept((T) this);
    }

    private void write(PacketSnapshot packet) {
        PacketUtils.write(this.channel, this.version, packet);
    }

    public void updateText(String input) {
        this.input = input;
        if (this.indicateInvalid) {
            this.invalidityReason = this.goal.getInvalidityReason(input);
            this.isPasswordValid = this.invalidityReason == null;
        }

        if (!input.isEmpty()) this.spoofValidAccordingly();
        //otherwise already spoofed
    }

    private void spoofValid() {
        this.writeAndFlush(this.validItems);
    }

    private void spoofInvalid() {
        this.writeAndFlush(this.invalidItems);
    }

    protected void spoofValidAccordingly() {
        if (this.isPasswordValid) this.spoofValid();
        else this.spoofInvalid();
    }

    public abstract void select(int slot);

    public final void open() {
        this.write(this.openInv);
        this.spoofValidAccordingly();
    }
}