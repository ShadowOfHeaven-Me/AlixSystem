package shadow.utils.objects.savable.data.gui;

import alix.common.data.LoginType;
import alix.common.login.skull.SkullTextureType;
import alix.common.login.skull.SkullTextures;
import alix.common.messages.Messages;
import alix.common.utils.formatter.AlixFormatter;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.constructors.OutSoundPacketConstructor;
import shadow.utils.misc.version.AlixMaterials;
import shadow.utils.objects.savable.data.gui.bedrock.VerificationBedrockGUI;
import shadow.utils.objects.savable.data.gui.builders.VirtualAnvilPasswordBuilder;
import shadow.utils.objects.savable.data.gui.builders.VirtualPinBuilder;
import shadow.utils.objects.savable.data.gui.builders.auth.UnverifiedVirtualAuthBuilder;
import shadow.utils.users.types.UnverifiedUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PasswordGui {

    public static final ItemStack BACKGROUND_ITEM;
    public static final com.github.retrooper.packetevents.protocol.item.ItemStack RETROOPER_BACKGROUND_ITEM;

    static {
        String config = Main.config.getString("background-item").toUpperCase();
        Material m;
        try {
            m = Material.valueOf(config);
        } catch (Exception e) {
            try {
                m = Material.valueOf("GRAY_STAINED_GLASS_PANE");
                Main.logWarning("Invalid material type in 'background-item': " + config + ". Defaulting to GRAY_STAINED_GLASS_PANE!");
            } catch (Throwable ignored) {//fix for lower versions
                m = Material.AIR;
            }
        }

        BACKGROUND_ITEM = rename(new ItemStack(m), "§f");
        RETROOPER_BACKGROUND_ITEM = SpigotConversionUtil.fromBukkitItemStack(BACKGROUND_ITEM);
    }

    public static final ByteBuf
            villagerNoSoundPacket = OutSoundPacketConstructor.constructConstUnverified(Sounds.ENTITY_VILLAGER_NO),
            playerLevelUpSoundPacket = OutSoundPacketConstructor.constructConstUnverified(Sounds.ENTITY_PLAYER_LEVELUP),
            itemBreakSoundPacket = OutSoundPacketConstructor.constructConstUnverified(Sounds.ENTITY_ITEM_BREAK),
            noteBlockSnareSoundPacket = OutSoundPacketConstructor.constructConstUnverified(Sounds.BLOCK_NOTE_BLOCK_SNARE),
            noteBlockHarpSoundPacket = OutSoundPacketConstructor.constructConstUnverified(Sounds.BLOCK_NOTE_BLOCK_HARP);

    public static final TextComponent
            //pinGUITitle = Component.text(Messages.get("pin-gui-title")),
            guiTitleLogin = Component.text(Messages.get("gui-title-login")),
            guiTitleRegister = Component.text(Messages.get("gui-title-register"));

    public static final String
            pinConfirm = Messages.get("pin-confirm"),
            pinRemoveLast = Messages.get("pin-remove-last"),
            pinReset = Messages.get("pin-reset"),
            pinLeave = Messages.get("pin-leave"),
            pinLeaveFeedback = Messages.get("pin-leave-feedback"),
            pinInvalidLength = Messages.get("pin-invalid-length"),
            invalidPassword = Messages.get("password-invalid-gui");

    public static final ItemStack[] digits = getDigits();
    public static final com.github.retrooper.packetevents.protocol.item.ItemStack[] retrooperDigits = toRetrooperItems(getDigits());

    public static final ItemStack BARRIER = rename(new ItemStack(Material.BARRIER), "§f");
    public static final com.github.retrooper.packetevents.protocol.item.ItemStack RETROOPER_BARRIER = SpigotConversionUtil.fromBukkitItemStack(BARRIER);
    public static final int[] EMPTY_DIGIT_SLOTS = new int[]{13, 14, 15, 16};
    public static final int FIRST_EMPTY_DIGIT_SLOT = EMPTY_DIGIT_SLOTS[0];
    public static final int[] PIN_DIGIT_SLOTS = new int[]{28, 0, 1, 2, 9, 10, 11, 18, 19, 20};
    public static final int
            ACTION_PIN_CONFIRM = 22,
            ACTION_LAST_REMOVE = 23,
            ACTION_RESET = 24,
            ACTION_LEAVE = 25;

    public static final ItemStack
            PIN_CONFIRM_ITEM = rename(AlixMaterials.GREEN_WOOL.getItemCloned(), pinConfirm),
            PIN_LAST_REMOVE_ITEM = rename(AlixMaterials.YELLOW_WOOL.getItemCloned(), pinRemoveLast),
            PIN_RESET_ITEM = rename(AlixMaterials.RED_WOOL.getItemCloned(), pinReset),
            PIN_LEAVE_ITEM = rename(AlixMaterials.BLACK_WOOL.getItemCloned(), pinLeave);

    private static final ItemStack[] pinVerificationGuiItems = createPINVerificationItems();
    public static final List<com.github.retrooper.packetevents.protocol.item.ItemStack> retrooperPinVerificationGuiItems = Arrays.asList(toRetrooperItems(pinVerificationGuiItems));

    public static com.github.retrooper.packetevents.protocol.item.ItemStack[] toRetrooperItems(ItemStack[] bukkit) {
        com.github.retrooper.packetevents.protocol.item.ItemStack[] retrooper = new com.github.retrooper.packetevents.protocol.item.ItemStack[bukkit.length];
        for (int i = 0; i < bukkit.length; i++) retrooper[i] = SpigotConversionUtil.fromBukkitItemStack(bukkit[i]);
        return retrooper;
    }

    public static List<com.github.retrooper.packetevents.protocol.item.ItemStack> toRetrooperItems(List<ItemStack> bukkit) {
        List<com.github.retrooper.packetevents.protocol.item.ItemStack> retrooper = new ArrayList<>(bukkit.size());
        for (ItemStack itemStack : bukkit) retrooper.add(SpigotConversionUtil.fromBukkitItemStack(itemStack));
        return retrooper;
    }

    public static AlixVerificationGui newBuilder(UnverifiedUser user, LoginType type) {
        AlixVerificationGui builder = create(user, type);
        //user.setGUIInitialized(true);
        return builder;
    }

    public static AlixVerificationGui newBuilder2FA(UnverifiedUser user) {
        AlixVerificationGui builder = new UnverifiedVirtualAuthBuilder(user);
        //user.setGUIInitialized(true);
        return builder;
    }

    public static AlixVerificationGui newBuilderBedrock(UnverifiedUser user, @NotNull Object bedrockPlayer) {
        AlixVerificationGui builder = new VerificationBedrockGUI(user, bedrockPlayer);
        //user.setGUIInitialized(true);
        return builder;
    }

    private static AlixVerificationGui create(UnverifiedUser user, LoginType type) {
        switch (type) {
            case PIN:
                return new VirtualPinBuilder(user);
            case ANVIL:
                return new VirtualAnvilPasswordBuilder(user);
            default:
                throw new AssertionError("Invalid: " + type);
        }
    }

    /*private static PasswordBuilder create(BuilderType type) {
        PersistentUserData data = user.getData();
        if (AlixUtils.defaultPasswordType == PasswordType.PIN || data != null && data.getPasswordType() == PasswordType.PIN) return new SimplePinBuilder(user);
        if (AlixUtils.anvilPasswordGui) return new AnvilPasswordBuilder(user);
        throw new AssertionError("Invalid: " + AlixUtils.defaultPasswordType);
    }*/

/*    public static PinBuilder newSyncBuilder(UnverifiedUser user) {
        PinBuilder pinBuilder = new PinBuilder();
        JavaScheduler.sync(() -> user.getPlayer().openInventory(pinBuilder.getGUI()));
        user.setPinGUIInitialized(true);
        return pinBuilder;
    }*/

/*    public static boolean recognizePin(String password) {
        if (password.length() != 4) return false;
        for (char c : password.toCharArray())
            if (c < 48 || c > 57) return false;
        return true;
    }

    public static void showPinGui(HumanEntity en) {
        JavaScheduler.async(() -> en.openInventory(getCloned()));
    }*/

/*    private static Inventory createNew(String title) {
        return Bukkit.createInventory(null, 36, title);
    }*/

/*    public static Inventory getPinGuiCloned(String title) {
        Inventory cloned = createNew(title);
        cloned.setContents(pinVerificationGuiItems);
        return cloned;
    }*/

    private static ItemStack[] createPINVerificationItems() {
        //Inventory inv = createNew(null);//we do not care about the title
        ItemStack[] items = new ItemStack[36];
        Arrays.fill(items, BACKGROUND_ITEM);

/*        for (byte i = 0; i < 36; i++) {
            ItemStack item = items[i];
            if (item == null || AlixMaterials.isAir(item.getType())) items[i] = BACKGROUND_ITEM;
        }*/

        for (byte i = 0; i <= 9; i++) items[PIN_DIGIT_SLOTS[i]] = digits[i];

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

    private static ItemStack[] getDigits() {
        String itemType = Main.config.getString("pin-digit-item-type");
        String itemAmountParam = Main.config.getString("pin-digit-item-amount");
        String itemAmountZeroParam = Main.config.getString("pin-digit-zero-item-amount");

        int itemAmountZero = 1;

        try {
            itemAmountZero = Integer.parseInt(itemAmountZeroParam);
        } catch (NumberFormatException e) {
            Main.logWarning("Invalid parameter set pin-digit-zero-item-amount. Expected a valid number, but instead got '" + itemAmountParam + "'!. '1' Will be used instead, as default");
        }

        boolean digitEquivalent = itemAmountParam.equals("digit_equivalent");

        int itemAmount = 1;

        if (!digitEquivalent) {

            try {
                itemAmount = Integer.parseInt(itemAmountParam);
            } catch (NumberFormatException e) {
                Main.logWarning("Invalid parameter set at pin-digit-item-amount - '" + itemAmountParam + "'!. It is neither 'digit_equivalent' nor a valid number! '1' Will be used instead, as default");
            }
        }


        ItemStack[] a = new ItemStack[10];

        Material m = null;

        boolean validMaterial;

        try {
            m = Material.valueOf(itemType.toUpperCase());
            validMaterial = true;
        } catch (Exception e) {
            validMaterial = false;
        }

        boolean skull = SkullTextureType.isSkull(itemType);

        if (!validMaterial) {
            if (!skull) {
                Main.logWarning("Invalid argument at pin-digit-item-type in the config - The given parameter " + itemType
                        + " is neither a built-in version nor a valid material! The built-in version 'quartz_skull' will be used instead, as default.");
                itemType = "quartz_skull";
            }
            skull = true;
        }

        for (byte i = 0; i < 10; i++) {
            ItemStack item = skull ? ofDigit(i, itemType) : new ItemStack(m);
            if (i == 0) item.setAmount(itemAmountZero);
            else item.setAmount(digitEquivalent ? i : itemAmount);
            a[i] = rename(item, "&e" + i);
        }
        return a;
    }

    private static ItemStack rename(ItemStack i, String s) {
        ItemMeta meta = i.getItemMeta();
        if (meta == null) return i;//ignore air
        meta.setDisplayName(AlixFormatter.translateColors(s));
        i.setItemMeta(meta);
        return i;
    }

    private static ItemStack ofDigit(byte digit, String skullType) {
        String encodedHead = SkullTextures.encodeSkullProperty(digit, SkullTextureType.parseSafe(skullType));

        return AlixUtils.getSkull("&e" + digit, encodedHead);
    }
}