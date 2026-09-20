package ua.nanit.limbo.connection.login.gui;

import alix.common.login.skull.SkullTextures;
import alix.common.messages.Messages;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import io.netty.channel.Channel;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryItems;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.function.Consumer;

import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.of;
import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.ofSkull;

public abstract class AbstractAnvilBuilder<T extends AbstractAnvilBuilder> {

    //Was hardcoded English literals ("&6Go back", "&7Leave", "&aConfirm", "&cInvalid password"), bypassing
    //the Messages/i18n system entirely - unlike RECOVER_ITEM just below, which already went through
    //Messages.get correctly. Now routed through the same lang-key mechanism so these follow the selected
    //"language" config option instead of always showing English.
    public static final ItemStack GO_BACK_ITEM = ofSkull(Messages.get("gui-go-back"), SkullTextures.GO_BACK);
    public static final ItemStack RECOVER_ITEM = of(ItemTypes.PAPER, Messages.get("gui-recover-account"));
    public static final int RECOVER_SLOT = 8;
    private static final String USER_INPUT_STR = "";
    private static final ItemStack[] itemsValidWithCancel, itemsValidWithLeave, itemsInvalidWithCancel, itemsInvalidWithLeave;

    static {
        ItemStack USER_INPUT = of(ItemTypes.PAPER, USER_INPUT_STR);
        ItemStack LEAVE_BUTTON = of(ItemTypes.BLACK_WOOL, Messages.get("gui-leave"));
        ItemStack CONFIRM_BUTTON = of(ItemTypes.LIME_WOOL, Messages.get("gui-confirm"));
        ItemStack INVALID_PASSWORD = of(ItemTypes.RED_WOOL, Messages.get("gui-invalid-password"));
        ItemStack CANCEL = GO_BACK_ITEM;

        itemsValidWithCancel = new ItemStack[]{USER_INPUT, CANCEL, CONFIRM_BUTTON};
        itemsValidWithLeave = new ItemStack[]{USER_INPUT, LEAVE_BUTTON, CONFIRM_BUTTON};

        itemsInvalidWithCancel = new ItemStack[]{USER_INPUT, CANCEL, INVALID_PASSWORD};
        itemsInvalidWithLeave = new ItemStack[]{USER_INPUT, LEAVE_BUTTON, INVALID_PASSWORD};
    }

    private static ItemStack[] create39Items(ItemStack[] top3, boolean includeRecover) {
        ItemStack[] items = new ItemStack[includeRecover ? 39 : 3];
        System.arraycopy(top3, 0, items, 0, 3);
        if (includeRecover) {
            for (int i = 3; i < 39; i++) items[i] = ItemStack.EMPTY;
            items[RECOVER_SLOT] = RECOVER_ITEM;
        }
        return items;
    }

    private final Channel channel;
    private final Version version;
    private final PacketSnapshot validItems, invalidItems, openInv;
    private final AnvilBuilderGoal goal;
    private final boolean indicateInvalid;
    private final Consumer<T> flush;
    private final CipherHandler cipher;

    protected AbstractAnvilBuilder(Channel channel, Version version, AnvilBuilderGoal goal, Consumer<T> flush) {
        this(channel, version, goal, flush, false);
    }

    protected AbstractAnvilBuilder(Channel channel, Version version, AnvilBuilderGoal goal, Consumer<T> flush, boolean hasRecoveryButton) {
        this.channel = channel;
        this.version = version;
        this.indicateInvalid = goal.indicateInvalid();
        this.isPasswordValid = !indicateInvalid;//it's empty right now, so invalid if we validate that
        this.goal = goal;

        ItemStack[] baseValid = goal.isUserVerified() ? itemsValidWithCancel : itemsValidWithLeave;
        ItemStack[] baseInvalid = goal.isUserVerified() ? itemsInvalidWithCancel : itemsInvalidWithLeave;

        this.validItems = new PacketPlayOutInventoryItems(create39Items(baseValid, hasRecoveryButton)).toSnapshot();
        this.invalidItems = new PacketPlayOutInventoryItems(create39Items(baseInvalid, hasRecoveryButton)).toSnapshot();
        this.openInv = goal.getInvOpen();
        this.flush = flush;
        this.cipher = CipherHandler.encryptionFor(this.channel);
    }

    protected String input = "";
    protected String invalidityReason;
    protected boolean isPasswordValid;

    private void writeAndFlush(PacketSnapshot packet) {
        this.write(packet);
        this.flush.accept((T) this);
    }

    private void write(PacketSnapshot packet) {
        /*if (true) {
            Log.error("CIPHER= " + this.cipher);
            return;
        }*/
        //Log.error("CIPHER= " + this.cipher);
        PacketUtils.write(this.channel, this.version, packet, this.cipher);
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