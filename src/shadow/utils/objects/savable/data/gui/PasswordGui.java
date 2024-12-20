package shadow.utils.objects.savable.data.gui;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import alix.common.utils.formatter.AlixFormatter;
import alix.libs.com.github.retrooper.packetevents.protocol.sound.Sounds;
import alix.libs.io.github.retrooper.packetevents.util.SpigotConversionUtil;
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
    public static final alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack RETROOPER_BACKGROUND_ITEM;

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
    public static final alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack[] retrooperDigits = toRetrooperItems(getDigits());

    public static final ItemStack BARRIER = rename(new ItemStack(Material.BARRIER), "§f");
    public static final alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack RETROOPER_BARRIER = SpigotConversionUtil.fromBukkitItemStack(BARRIER);
    public static final int[] EMPTY_DIGIT_SLOTS = new int[]{13, 14, 15, 16};
    public static final int FIRST_EMPTY_DIGIT_SLOT = EMPTY_DIGIT_SLOTS[0];
    public static final int[] PIN_DIGIT_SLOTS = new int[]{28, 0, 1, 2, 9, 10, 11, 18, 19, 20};
    //public static final Map<Integer, Byte>
    public static final int //slots, an array in the future maybe? - Yes.
/*          DIGIT_0 = 28,
            DIGIT_1 = 0,
            DIGIT_2 = 1,
            DIGIT_3 = 2,
            DIGIT_4 = 9,
            DIGIT_5 = 10,
            DIGIT_6 = 11,
            DIGIT_7 = 18,
            DIGIT_8 = 19,
            DIGIT_9 = 20,*/
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
    public static final List<alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack> retrooperPinVerificationGuiItems = Arrays.asList(toRetrooperItems(pinVerificationGuiItems));

    public static alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack[] toRetrooperItems(ItemStack[] bukkit) {
        alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack[] retrooper = new alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack[bukkit.length];
        for (int i = 0; i < bukkit.length; i++) retrooper[i] = SpigotConversionUtil.fromBukkitItemStack(bukkit[i]);
        return retrooper;
    }

    public static List<alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack> toRetrooperItems(List<ItemStack> bukkit) {
        List<alix.libs.com.github.retrooper.packetevents.protocol.item.ItemStack> retrooper = new ArrayList<>(bukkit.size());
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

        boolean skull = false;

        switch (itemType) {
            case "wooden_skull":
            case "chat_stone_skull":
            case "plush_skull":
            case "quartz_skull":
                skull = true;
        }

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
        String encodedHead = encodeSkullProperty(digit, skullType);

        return AlixUtils.getSkull("&e" + digit, encodedHead);
    }

    private static String encodeSkullProperty(byte digit, String skullType) {
        switch (skullType) {
            case "wooden_skull":
                return encoded_WoodenDigitSkullProperty(digit);
            case "chat_stone_skull":
                return encoded_ChatStoneSkullProperty(digit);
            case "plush_skull":
                return encoded_PlushSkullProperty(digit);
            case "quartz_skull":
                return encoded_QuartzSkullProperty(digit);
        }
        throw new AssertionError("Invalid: " + skullType);
    }

    private static String encoded_QuartzSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY4ODZkOWM0MGVmN2Y1MGMyMzg4MjQ3OTJjNDFmYmZiNTRmNjY1ZjE1OWJmMWJjYjBiMjdiM2VhZDM3M2IifX19";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBhMTllMjNkMjFmMmRiMDYzY2M1NWU5OWFlODc0ZGM4YjIzYmU3NzliZTM0ZTUyZTdjN2I5YTI1In19fQ==";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M1OTZhNDFkYWVhNTFiZTJlOWZlYzdkZTJkODkwNjhlMmZhNjFjOWQ1N2E4YmRkZTQ0YjU1OTM3YjYwMzcifX19";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg1ZDRmZGE1NmJmZWI4NTEyNDQ2MGZmNzJiMjUxZGNhOGQxZGViNjU3ODA3MGQ2MTJiMmQzYWRiZjVhOCJ9fX0=";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg1MmEyNWZlNjljYTg2ZmI5ODJmYjNjYzdhYzk3OTNmNzM1NmI1MGI5MmNiMGUxOTNkNmI0NjMyYTliZDYyOSJ9fX0=";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlZTdkOTU0ZWIxNGE1Y2NkMzQ2MjY2MjMxYmY5YTY3MTY1MjdiNTliYmNkNzk1NmNlZjA0YTlkOWIifX19";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY4MmEzYWU5NDgzNzRlMDM3ZTNkN2RkNjg3ZDU5ZDE4NWRkMmNjOGZjMDlkZmViNDJmOThmOGQyNTllNWMzIn19fQ==";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGVhMzBjMjRjNjBiM2JjMWFmNjU4ZWY2NjFiNzcxYzQ4ZDViOWM5ZTI4MTg4Y2Y5ZGU5ZjgzMjQyMmU1MTAifX19";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZhYmFmZDAyM2YyMzBlNDQ4NWFhZjI2ZTE5MzY4ZjU5ODBkNGYxNGE1OWZjYzZkMTFhNDQ2Njk5NDg5MiJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ3OTEwZTEwMzM0Zjg5MGE2MjU0ODNhYzBjODI0YjVlNGExYTRiMTVhOTU2MzI3YTNlM2FlNDU4ZDllYTQifX19";
        }
        throw new AssertionError("Invalid: " + digit);
    }


    private static String encoded_PlushSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYzZDE5NzJjZTJmMmI3OWIyZGYwN2FkNWM1NzRmOWNjZDBlY2ViMjA0MTQzZGY5NjZkMzAxYjE4ZTkyYjBiNiJ9fX0=";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTBhNGM5NGRkNjVhN2NhZGM0MTcyY2VkZjM5OTBhODU0Mjc3MmJiMTEzY2RmMWEwZjc4ZWY2NTJjNmFiZTZjYiJ9fX0=";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ3YzU1MDY0ZjFkMDc2MjVhZjA1Y2RiYzBmYzdjODQxNzNiMTA1NDEwMjRjODg1NWVkNzEzMmNhNzc2NzI0NiJ9fX0=";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDljYjk0ZDg3MjkxOTdhMThhNzY4ODJjMjQ5MThhOGMwNDQ5NGQzNDcyN2Y0ZGYwNjBmYjdhNDVhMWZmODUzNyJ9fX0=";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzZhZDc1NjVmYTY4OTQyZDJmZjVmMDRlZmRhODBkOWU5Zjg4ZDMzNDAxNjgwZjE4MmUwZjM3YWYzMmI4YzI5NCJ9fX0=";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAyMDNiMTI0YWNlZmQwOGM0NDZhMDQ5MmE1YzlkODAwZWUwY2Q4YzI2MGMzYzEyMzc3NzMxNjU5MjUxYWQ0ZSJ9fX0=";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTQ5MTAxN2ZkNTI2N2ZjOWE2NDFjY2UwMjUwZDQ2ODM4MDRlM2IxMzk5YjJiNWI1OGYxMzBmNTc3NGRlMjgyMSJ9fX0=";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQyNWFjYzQyY2FmOGIxZTk1Y2NiMTZkMzc5YWYwYjc2Zjk1ZWQyOWVmMmE0OTQwNzNkMGIwN2IxNWRjMjJmZCJ9fX0=";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjQ5MDRkNWRlZTM3ODJkNGQ2YzZmZWJlNzI3ODNkOTYxNDgyNzdlZmJlNzhjMTUxNGNjMjAwODhkOGZmMGQwOSJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFkYzY1YzNiODk2YWI2NmRmMWVkN2JhNDI1MGQxNmJiODM1MmE2ZDI4NWE1NGMwMDIxOGVhNzJhNGJiNzE0OSJ9fX0=";
        }
        throw new AssertionError("Invalid: " + digit);
    }

    private static String encoded_ChatStoneSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmI4N2I0NDExMWE5ZjMzOWVkNzAxNWQwZjJjYjY0NmNlNmI4YzU5YTBiNzUwYjI3MjQ0OWFlYWMyNTYyNWRmYSJ9fX0=";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJkNGE2OTkzN2UwYmVhZGMzODQyNmMwOTk0YjUwZDk1MDQwNmZkOGRhOWYzMWM1ODJkNDZmM2I5YmZjNGM1YiJ9fX0=";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzBhNmM3YTBkNjU4YmI5MGUyN2I1OTM0ZjYyYTVlMTVjYzljMTFjODdhZTE0NjRhNGU3OWVhNjY1MjNiYTM2MSJ9fX0=";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYxYjMxYTg3Yjc4MjYyYzYzZTk0NzE0ZTU2MjRhMmFiNTk1MGY3NWRlZTMyY2MzMDI2YTVmYTc4MjM0NjhkZSJ9fX0=";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FkZmQzYzk5OTY3ZDMyNzQ5MDJlY2I2ZTk4NjU4YWNmZGIzOTE4NzE3YjJlOTAzN2Y2MWMzYjRlMDllMmExIn19fQ==";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGJiYWYwMTkwOTIyMWI5YWJlOTQ1YWZlN2RmZGI3MmYzMTczMzExZTU2MjAxOTRkZDI3MDExYTZkNTU0ZmZjOCJ9fX0=";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZhZTBmZTIyNTZhZTM1NmEyNWYxMzBhZTcxY2Y0NDMxNTE1N2M1ZmFlOTFkNjJhNGZmYjU4NWIxNjQ4NjM3MyJ9fX0=";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGYwOWVmZTczMWU3M2M4MGIxYWVlMTAwYmIzMzBhYjQxNDU5NWVlNTRhNGUyZGVjNDM5YmVkM2UzNjQ5YWM5NiJ9fX0=";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDcyN2Q0ZTQ4ZjIzMWNlNGQ4NzE5OTI1NjBmNTFiZjZhM2YxNTdjMmZmZDZmOTJiODYwY2JiNTMxMjg0MjZhMiJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ3M2VhMzUxZjQxZTk5NTdmOTE0ZTNiOTBmNzRlOTg2NzgzOGIzMzM5ZDQyNTEzY2EyNWVkMGY0NWJjNjBjYiJ9fX0=";
        }
        throw new AssertionError("Invalid: " + digit);
    }

    private static String encoded_WoodenDigitSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGViZTdlNTIxNTE2OWE2OTlhY2M2Y2VmYTdiNzNmZGIxMDhkYjg3YmI2ZGFlMjg0OWZiZTI0NzE0YjI3In19fQ==";
            case 1:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=";
            case 2:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19";
            case 3:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19";
            case 4:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlNzhmYjIyNDI0MjMyZGMyN2I4MWZiY2I0N2ZkMjRjMWFjZjc2MDk4NzUzZjJkOWMyODU5ODI4N2RiNSJ9fX0=";
            case 5:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ1N2UzYmM4OGE2NTczMGUzMWExNGUzZjQxZTAzOGE1ZWNmMDg5MWE2YzI0MzY0M2I4ZTU0NzZhZTIifX19";
            case 6:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM0YjM2ZGU3ZDY3OWI4YmJjNzI1NDk5YWRhZWYyNGRjNTE4ZjVhZTIzZTcxNjk4MWUxZGNjNmIyNzIwYWIifX19";
            case 7:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRiNmViMjVkMWZhYWJlMzBjZjQ0NGRjNjMzYjU4MzI0NzVlMzgwOTZiN2UyNDAyYTNlYzQ3NmRkN2I5In19fQ==";
            case 8:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTkxOTQ5NzNhM2YxN2JkYTk5NzhlZDYyNzMzODM5OTcyMjI3NzRiNDU0Mzg2YzgzMTljMDRmMWY0Zjc0YzJiNSJ9fX0=";
            case 9:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3Y2FmNzU5MWIzOGUxMjVhODAxN2Q1OGNmYzY0MzNiZmFmODRjZDQ5OWQ3OTRmNDFkMTBiZmYyZTViODQwIn19fQ==";
        }
        throw new AssertionError("Invalid: " + digit);
    }
}