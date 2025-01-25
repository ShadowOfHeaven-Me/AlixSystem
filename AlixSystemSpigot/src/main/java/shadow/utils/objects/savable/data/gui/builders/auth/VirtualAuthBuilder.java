package shadow.utils.objects.savable.data.gui.builders.auth;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.data.LoginType;
import alix.common.login.auth.GoogleAuthUtils;
import alix.common.messages.Messages;
import alix.common.utils.other.keys.secret.MapSecretKey;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.AlixInventoryType;
import shadow.utils.objects.savable.data.gui.AlixJavaVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.objects.savable.data.gui.virtual.CachingVirtualInventory;
import shadow.utils.objects.savable.data.gui.virtual.VirtualInventory;
import shadow.utils.users.types.AlixUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class VirtualAuthBuilder implements AlixJavaVerificationGui {

    private static final String START_TEXT;
    private static final String[] EMPTY_SLOTS_TEXTS;

    static {
        START_TEXT = Messages.get("google-auth-gui-title") + " "; //repeat(' ', 10);

        EMPTY_SLOTS_TEXTS = new String[7];
        for (int i = 0; i <= 6; i++) {
            EMPTY_SLOTS_TEXTS[i] = repeat('_', i);
        }
    }

    private static String repeat(char c, int times) {
        char[] a = new char[times];
        Arrays.fill(a, c);
        return new String(a);
    }

    //private static final ByteBuf constCloseInv = NettyUtils.constBuffer(new WrapperPlayServerCloseWindow(VirtualInventory.CONST_WINDOW_ID));
    /*private static final Location retrooperTpLoc = SpigotConversionUtil.fromBukkitLocation(GoogleAuth.QR_CODE_TP_LOC);
    private static final ByteBuf
            villagerNoSoundPacket = OutSoundPacketConstructor.constructConst(Sounds.ENTITY_VILLAGER_NO, retrooperTpLoc),
            playerLevelUpSoundPacket = OutSoundPacketConstructor.constructConst(Sounds.ENTITY_PLAYER_LEVELUP, retrooperTpLoc),
            itemBreakSoundPacket = OutSoundPacketConstructor.constructConst(Sounds.ENTITY_ITEM_BREAK, retrooperTpLoc),
            noteBlockSnareSoundPacket = OutSoundPacketConstructor.constructConst(Sounds.BLOCK_NOTE_BLOCK_SNARE, retrooperTpLoc),
            noteBlockHarpSoundPacket = OutSoundPacketConstructor.constructConst(Sounds.BLOCK_NOTE_BLOCK_HARP, retrooperTpLoc);*/
    private static final VirtualItem[] ITEMS;
    private static final ByteBuf invItemsByteBuf, invItemsByteBufNoLeave;
    private static final ByteBuf invOpenBuffer = CachingVirtualInventory.constInvOpenByteBuf(36, Component.text(START_TEXT + "______"));
    private static final int LEAVE_BUTTON_INDEX = 33;
    private final AlixUser user;
    private final String hexSecretKey;
    private final CachingVirtualInventory gui;
    private final StringBuilder digits = new StringBuilder(6);
    private final Consumer<Boolean> onConfirm;
    private final boolean includeLeaveButton;

    static {
        ITEMS = new VirtualItem[36];
        VirtualItem BACKGROUND = new VirtualItem(PasswordGui.RETROOPER_BACKGROUND_ITEM);
        Arrays.fill(ITEMS, BACKGROUND);

        for (byte digit = 0; digit <= 9; digit++) {
            int slot = slotOfDigit(digit);
            VirtualItem item = ofDigit(digit);
            ITEMS[slot] = item;
        }

        //https://www.google.com/search?sca_esv=00358603c58a2193&sca_upv=1&sxsrf=ADLYWII8F4utREotT2qyDMm82eTw01l7VA:1723997870585&q=minecraft+inventory+9x4+slots+ids&udm=2&fbs=AEQNm0Dvr3xYvXRaGaB8liPABJYdGovAUMem85jmaNP43N9LWkpJzPExVfN1dM6Qi4rL-ZCUpN5qLHKOqAlKUxwZpsmlou8qLwHyBOcWJHi8Pqxo1W88SIdb0PybIFRbYs4nudg0PPTPajHvq61RuUPwd52wcUKqSH_Nw8Y28t064byqq8h1g0vsT9v4iiwluAasdriw-t2D&sa=X&ved=2ahUKEwjawa3F-P6HAxUuAxAIHYjLJ5QQtKgLegQIDhAB&biw=1912&bih=956&dpr=1#vhid=axckUtW3KoGxcM&vssid=mosaic
        ITEMS[6] = new VirtualItem(PasswordGui.PIN_CONFIRM_ITEM, VirtualAuthBuilder::onConfirm);

        ITEMS[15] = new VirtualItem(PasswordGui.PIN_LAST_REMOVE_ITEM, VirtualAuthBuilder::removeLast);

        ITEMS[24] = new VirtualItem(PasswordGui.PIN_RESET_ITEM, VirtualAuthBuilder::resetInput);

        ITEMS[LEAVE_BUTTON_INDEX] = new VirtualItem(PasswordGui.PIN_LEAVE_ITEM, builder -> {
            MethodProvider.kickAsync(builder.user, pinLeaveFeedbackKickPacket);
        });

        invItemsByteBuf = CachingVirtualInventory.constInvItemsByteBuf(newListOfItems(true));
        invItemsByteBufNoLeave = CachingVirtualInventory.constInvItemsByteBuf(newListOfItems(false));
    }

    private static VirtualItem ofDigit(byte digit) {
        ItemStack item = PasswordGui.retrooperDigits[digit];
        return new VirtualItem(item, builder -> builder.appendDigit(digit));
    }

    private void onConfirm() {
        if (this.digits.length() != 6) {
            this.playSoundOnDenial();
            return;
        }
        boolean isValid = GoogleAuthUtils.isValid(this.hexSecretKey, this.digits.toString());
        if (isValid) this.playSoundOnSuccess();
        else this.resetInput();
        this.onConfirm.accept(isValid);
    }

    private void removeLast() {
        if (this.digits.length() == 0) {
            //this.gui.setSpoofWithCached(true);
            return;
        }
        this.digits.deleteCharAt(this.digits.length() - 1);
        //if (this.digits.length() == 0) this.gui.setSpoofWithCached(true);

        this.playSoundOnLastRemove();
        this.refreshTitle();
    }

    private void resetInput() {
        if (this.digits.length() == 0) {
            //this.gui.setSpoofWithCached(true);
            return;
        }
        this.digits.setLength(0);
        //this.user.writeConstSilently(constCloseInv);
        this.playSoundOnAllReset();
        this.refreshTitle();
        //this.gui.setSpoofWithCached(true);
    }

    private void appendDigit(byte digit) {
        if (this.digits.length() == 6) return;
        this.digits.append(digit);
        this.playSoundOnDigitAppend();
        //this.digits[index++] = (char) (digit + 48);//48 is '0' is ASCII

        this.refreshTitle();
    }

    private void refreshTitle() {
        //this.user.writeConstSilently(constCloseInv);
        int index = 6 - this.digits.length();

        String title = START_TEXT + this.digits + EMPTY_SLOTS_TEXTS[index];
        ByteBuf invOpen = CachingVirtualInventory.invOpenByteBuf(AlixInventoryType.GENERIC_9X4, Component.text(title));
        this.user.writeAndFlushSilently(invOpen);
    }

    private static int slotOfDigit(byte digit) {
        switch (digit) {
            case 0:
                return 29;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 10;
            case 5:
                return 11;
            case 6:
                return 12;
            case 7:
                return 19;
            case 8:
                return 20;
            case 9:
                return 21;
            default:
                throw new AlixError("Invalid: " + digit);
        }
    }

/*    private static final List<ItemStack>
            ITEM_LIST = ImmutableList.<ItemStack>builder().addAll(newListOfItems(true)).build(),
    ITEM_LIST_NO_LEAVE = ImmutableList.<ItemStack>builder().addAll(newListOfItems(true)).build();*/

    private static List<ItemStack> newListOfItems(boolean includeLeaveButton) {
        List<ItemStack> items = new ArrayList<>(ITEMS.length);
        for (VirtualItem i : ITEMS) items.add(i.item);
        if (!includeLeaveButton) items.set(LEAVE_BUTTON_INDEX, PasswordGui.RETROOPER_BACKGROUND_ITEM);
        return items;
    }

    public VirtualAuthBuilder(AlixUser user, Consumer<Boolean> onConfirm, boolean includeLeaveButton) {
        this.includeLeaveButton = includeLeaveButton;
        this.user = user;
        this.onConfirm = onConfirm;
        this.gui = new CachingVirtualInventory(this.user.silentContext(), null, includeLeaveButton ? invItemsByteBuf : invItemsByteBufNoLeave, invOpenBuffer);
        this.hexSecretKey = GoogleAuthUtils.getHexKey(UserTokensFileManager.getTokenOrSupply(MapSecretKey.uuidKey(user.retrooperUser().getUUID()), GoogleAuthUtils::generateSecretKey));
        this.gui.setSpoofWithCached(true);
    }

    @Override
    public void select(int slot) {
        if (slot < 0 || slot >= ITEMS.length) return;
        if (!this.includeLeaveButton && slot == LEAVE_BUTTON_INDEX) {
            this.gui.spoofAllItems();
            return;
        }

        VirtualItem item = ITEMS[slot];
        if (item.action != null) item.action.accept(this);

        this.gui.spoofAllItems();
        //this.gui.setSpoofWithCached(false);
        //this.user.writeAndFlushDynamicSilently(new WrapperPlayServerWindowItems(0, 0, ));
    }

    @Override
    @NotNull
    public VirtualInventory getVirtualGUI() {
        return this.gui;
    }

    @Override
    @NotNull
    public LoginType getType() {
        return LoginType.AUTH_2FA;
    }

    @Override
    @NotNull
    public String getPasswordBuilt() {
        return this.digits.toString();
    }

    abstract void playSoundOnSuccess();

    abstract void playSoundOnDenial();

    abstract void playSoundOnDigitAppend();

    abstract void playSoundOnLastRemove();

    abstract void playSoundOnAllReset();

    private static final class VirtualItem {

        private final ItemStack item;
        private final Consumer<VirtualAuthBuilder> action;

        private VirtualItem(ItemStack item) {
            this.item = item;
            this.action = null;//AlixCommonUtils.EMPTY_CONSUMER;
        }

        private VirtualItem(ItemStack item, Consumer<VirtualAuthBuilder> action) {
            this.item = item;
            this.action = action;
        }

        private VirtualItem(org.bukkit.inventory.ItemStack item, Consumer<VirtualAuthBuilder> action) {
            this.item = SpigotConversionUtil.fromBukkitItemStack(item);
            this.action = action;
        }
    }
}