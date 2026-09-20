package ua.nanit.limbo.connection.login.gui;

import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryItems;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryOpen;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

import java.util.Arrays;

import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.BACKGROUND_ITEM;
import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.of;

//The "Recover account?" item (LimboAnvilBuilder/LimboPinBuilder/the bedrock form) used to skip straight into
//a recovery method when only one was set up, but fell back to a plain clickable chat message (not an actual
//GUI) whenever an account had BOTH email and passkey recovery configured - requested by Shadow to be a real
//GUI instead. Kept as a minimal, non-anvil chest menu (like LimboPinBuilder) since neither choice needs text
//input here - only the follow-up email step does, which LimboRecoveryAnvilBuilder already handles.
public final class LimboRecoveryChoiceBuilder implements LimboGUI {

    private static final int EMAIL_SLOT = 3, PASSKEY_SLOT = 5, GO_BACK_SLOT = 8;

    private static final ItemStack
            EMAIL_ITEM = of(ItemTypes.PAPER, Messages.get("gui-recovery-choice-email"), Messages.get("gui-recovery-choice-email-lore")),
            PASSKEY_ITEM = of(ItemTypes.TRIPWIRE_HOOK, Messages.get("gui-recovery-choice-passkey"), Messages.get("gui-recovery-choice-passkey-lore"));

    private static final ItemStack[] ITEMS = createItems();

    private static final PacketSnapshot
            itemsPacket = new PacketPlayOutInventoryItems(ITEMS).toSnapshot(),
            invOpenPacket = PacketPlayOutInventoryOpen.snapshot(AlixInventoryType.GENERIC_9X1, Messages.get("gui-title-recovery-choice"));

    private static ItemStack[] createItems() {
        ItemStack[] items = new ItemStack[9];
        Arrays.fill(items, BACKGROUND_ITEM);
        items[EMAIL_SLOT] = EMAIL_ITEM;
        items[PASSKEY_SLOT] = PASSKEY_ITEM;
        items[GO_BACK_SLOT] = AbstractAnvilBuilder.GO_BACK_ITEM;
        return items;
    }

    private final PacketDuplexHandler duplexHandler;
    private final LoginState loginState;

    public LimboRecoveryChoiceBuilder(ClientConnection connection, LoginState loginState) {
        this.duplexHandler = connection.getDuplexHandler();
        this.loginState = loginState;
    }

    @Override
    public void select(int slot) {
        switch (slot) {
            case EMAIL_SLOT:
                this.loginState.openRecoveryEmailGui();
                return;
            case PASSKEY_SLOT:
                this.loginState.handleRecoveryPasskeyCommand();
                return;
            case GO_BACK_SLOT:
                this.loginState.reopenOriginalGui();
                return;
            default:
                this.duplexHandler.writeAndFlush(itemsPacket);
        }
    }

    @Override
    public void onCloseAttempt() {
        this.show();
    }

    @Override
    public void show() {
        this.duplexHandler.write(invOpenPacket);
        this.duplexHandler.writeAndFlush(itemsPacket);
    }
}
