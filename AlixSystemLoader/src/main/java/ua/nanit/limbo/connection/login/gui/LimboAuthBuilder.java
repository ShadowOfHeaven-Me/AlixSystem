package ua.nanit.limbo.connection.login.gui;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.login.auth.GoogleAuthUtils;
import alix.common.login.skull.SkullTextureType;
import alix.common.login.skull.SkullTextures;
import alix.common.messages.Messages;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.packets.inventory.InventoryWrapper;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.other.keys.secret.MapSecretKey;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.*;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.packets.SoundPackets;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryItems;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayOutInventoryOpen;

import java.util.*;
import java.util.function.Consumer;

public final class LimboAuthBuilder implements LimboGUI {

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

    public static final ItemStack BACKGROUND_ITEM = of(ItemTypes.GRAY_STAINED_GLASS_PANE, "§f");
    public static final ItemStack BARRIER = of(ItemTypes.BARRIER, "§f");
    private static final VirtualItem[] ITEMS;
    private static final PacketSnapshot invItems, invItemsNoLeave;

    private static final int LEAVE_BUTTON_INDEX = 33;
    private final String hexSecretKey;
    private final StringBuilder digits = new StringBuilder(6);
    private final Consumer<Boolean> onConfirm;
    private final boolean includeLeaveButton;

    public static final ItemStack[] DIGITS;
    public static final PacketSnapshot leaveFeedbackKickPacket = PacketPlayOutDisconnect.snapshot("§7Left.");

    static {
        DIGITS = new ItemStack[10];
        for (byte i = 0; i < 10; i++) {
            DIGITS[i] = itemOfDigit(i);
        }

        ITEMS = new VirtualItem[36];
        VirtualItem BACKGROUND = new VirtualItem(BACKGROUND_ITEM);
        Arrays.fill(ITEMS, BACKGROUND);

        for (byte digit = 0; digit <= 9; digit++) {
            int slot = slotOfDigit(digit);
            VirtualItem item = ofDigit(digit);
            ITEMS[slot] = item;
        }

        //https://www.google.com/search?sca_esv=00358603c58a2193&sca_upv=1&sxsrf=ADLYWII8F4utREotT2qyDMm82eTw01l7VA:1723997870585&q=minecraft+inventory+9x4+slots+ids&udm=2&fbs=AEQNm0Dvr3xYvXRaGaB8liPABJYdGovAUMem85jmaNP43N9LWkpJzPExVfN1dM6Qi4rL-ZCUpN5qLHKOqAlKUxwZpsmlou8qLwHyBOcWJHi8Pqxo1W88SIdb0PybIFRbYs4nudg0PPTPajHvq61RuUPwd52wcUKqSH_Nw8Y28t064byqq8h1g0vsT9v4iiwluAasdriw-t2D&sa=X&ved=2ahUKEwjawa3F-P6HAxUuAxAIHYjLJ5QQtKgLegQIDhAB&biw=1912&bih=956&dpr=1#vhid=axckUtW3KoGxcM&vssid=mosaic
        ITEMS[6] = new VirtualItem(ItemTypes.GREEN_WOOL, LimboAuthBuilder::onConfirm);

        ITEMS[15] = new VirtualItem(ItemTypes.YELLOW_WOOL, LimboAuthBuilder::removeLast);

        ITEMS[24] = new VirtualItem(ItemTypes.RED_WOOL, LimboAuthBuilder::resetInput);

        ITEMS[LEAVE_BUTTON_INDEX] = new VirtualItem(ItemTypes.BLACK_WOOL, builder -> {
            builder.connection.sendPacketAndClose(leaveFeedbackKickPacket);
        });

        invItems = new PacketPlayOutInventoryItems(newListOfItems(true)).toSnapshot();
        invItemsNoLeave = new PacketPlayOutInventoryItems(newListOfItems(false)).toSnapshot();
    }

    private static ItemStack itemOfDigit(byte digit) {
        String encoding = SkullTextures.encodeSkullProperty(digit, SkullTextureType.WOODEN_SKULL);

        return ofSkull("§e" + digit, encoding);
    }

    public static ItemStack ofSkull(String name, String url) {
        return profile(builderOf(ItemTypes.PLAYER_HEAD, name), url).build();
    }

    private static ItemStack.Builder profile(ItemStack.Builder builder, String url) {
        builder.component(ComponentTypes.PROFILE, new ItemProfile("sex", UUID.randomUUID(),
                Collections.singletonList(new ItemProfile.Property("textures", url, null))));

        NBTCompound nbt = builder.build().getNBT();
        if (nbt == null) nbt = new NBTCompound();

        NBTCompound skullOwner = new NBTCompound();

        skullOwner.setTag("Id", new NBTIntArray(new int[]{0, 12, 1, 1}));//for below 1.16.2 I think
        nbt.setTag("SkullOwner", skullOwner);

        NBTCompound properties = new NBTCompound();

        NBTCompound textureEntry = new NBTCompound();
        textureEntry.setTag("Value", new NBTString(url));

        NBTList textures = new NBTList(NBTType.COMPOUND);
        textures.addTag(textureEntry);
        properties.setTag("textures", textures);

        skullOwner.setTag("Properties", properties);
        return builder;
    }

    public static ItemStack of(ItemType type, String name) {
        return builderOf(type, name).build();
    }

    public static ItemStack of(ItemType type, String name, String... lore) {
        return builderOf(type, name, lore).build();
    }

    public static ItemLore getItemLore(String... lore) {
        return new ItemLore(Arrays.stream(lore).map(Component::text).map(t -> (Component) t).toList());
    }

    public static ItemStack.Builder builderOf(ItemType type, String name, String... lore) {
        return ItemStack.builder().type(type)
                .component(ComponentTypes.CUSTOM_NAME, Component.text(AlixFormatter.translateColors(name)))
                .component(ComponentTypes.LORE, getItemLore(lore));
    }


    public static ItemStack.Builder builderOf(ItemType type, String name) {
        return name(ItemStack.builder().type(type), name);
    }

    private static ItemStack.Builder name(ItemStack.Builder builder, String name) {
        name = AlixFormatter.translateColors(name);

        builder.component(ComponentTypes.CUSTOM_NAME, Component.text(name));

        NBTCompound display = new NBTCompound();
        display.setTag("Name", new NBTString("{\"text\":\"" + name + "\"}"));

        NBTCompound nbt = new NBTCompound();
        nbt.setTag("display", display);
        builder.nbt(nbt);

        return builder;
    }

    private static VirtualItem ofDigit(byte digit) {
        return new VirtualItem(DIGITS[digit], builder -> builder.appendDigit(digit));
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
        if (this.digits.isEmpty()) {
            //this.gui.setSpoofWithCached(true);
            return;
        }
        this.digits.deleteCharAt(this.digits.length() - 1);
        //if (this.digits.length() == 0) this.gui.setSpoofWithCached(true);

        this.playSoundOnLastRemove();
        this.refreshTitle();
    }

    private void resetInput() {
        if (this.digits.isEmpty()) {
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
        var invOpen = InventoryWrapper.createInvOpen(AlixInventoryType.GENERIC_9X4, Component.text(title), this.connection.getClientVersion().getRetrooperVersion().toClientVersion());

        this.duplexHandler.writeAndFlush(new PacketPlayOutInventoryOpen(invOpen, AlixInventoryType.GENERIC_9X4));
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
        if (!includeLeaveButton)
            items.set(LEAVE_BUTTON_INDEX, ItemStack.builder().type(ItemTypes.GRAY_STAINED_GLASS_PANE).build());
        return items;
    }

    private static final PacketSnapshot openInvPacket =
            PacketPlayOutInventoryOpen.snapshot(
                    AlixInventoryType.GENERIC_9X4,
                    START_TEXT + "______");
    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private final PacketSnapshot allItems;

    public LimboAuthBuilder(ClientConnection connection, MapSecretKey<UUID> secretKey, Consumer<Boolean> onConfirm, boolean includeLeaveButton) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        this.includeLeaveButton = includeLeaveButton;
        this.onConfirm = onConfirm;
        this.hexSecretKey = GoogleAuthUtils.getHexKey(UserTokensFileManager.getTokenOrSupply(secretKey, GoogleAuthUtils::generateSecretKey));
        this.allItems = includeLeaveButton ? invItems : invItemsNoLeave;
    }

    @Override
    public void show() {
        this.duplexHandler.write(openInvPacket);
        this.spoofAllItems();
    }

    @Override
    public void select(int slot) {
        if (slot < 0 || slot >= ITEMS.length) return;
        if (!this.includeLeaveButton && slot == LEAVE_BUTTON_INDEX) {
            this.spoofAllItems();
            return;
        }

        VirtualItem item = ITEMS[slot];
        if (item.action != null) item.action.accept(this);

        this.spoofAllItems();
    }

    @Override
    public void onCloseAttempt() {
        this.refreshTitle();
        this.spoofAllItems();
    }

    private void spoofAllItems() {
        this.duplexHandler.writeAndFlush(this.allItems);
    }

    private void playSoundOnSuccess() {
        this.duplexHandler.write(SoundPackets.PLAYER_LEVELUP);
    }

    private void playSoundOnDenial() {
        this.duplexHandler.write(SoundPackets.VILLAGER_NO);
    }

    private void playSoundOnDigitAppend() {
        this.duplexHandler.write(SoundPackets.NOTE_BLOCK_HARP);
    }

    private void playSoundOnLastRemove() {
        this.duplexHandler.write(SoundPackets.NOTE_BLOCK_SNARE);
    }

    private void playSoundOnAllReset() {
        this.duplexHandler.write(SoundPackets.ITEM_BREAK);
    }

    private static final class VirtualItem {

        final ItemStack item;
        final Consumer<LimboAuthBuilder> action;

        VirtualItem(ItemType type) {
            this(type, null);
        }

        VirtualItem(ItemType type, Consumer<LimboAuthBuilder> action) {
            this.item = ItemStack.builder().type(type).build();
            this.action = action;
        }

        VirtualItem(ItemStack item) {
            this.item = item;
            this.action = null;//AlixCommonUtils.EMPTY_CONSUMER;
        }

        VirtualItem(ItemStack item, Consumer<LimboAuthBuilder> action) {
            this.item = item;
            this.action = action;
        }
    }
}