package shadow.utils.objects.savable.data.password.builders;

import alix.common.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.password.builders.impl.AnvilPasswordBuilder;
import shadow.utils.objects.savable.data.password.builders.impl.SimplePinBuilder;
import shadow.utils.users.offline.UnverifiedUser;

public class PasswordGui {

    public static final String
            pinConfirm = Messages.get("pin-confirm"),
            pinRemoveLast = Messages.get("pin-remove-last"),
            pinReset = Messages.get("pin-reset"),
            pinLeave = Messages.get("pin-leave"),
            pinLeaveFeedback = Messages.get("pin-leave-feedback"),
            pinInvalidLength = Messages.get("pin-invalid-length"),
            pinGUITitle = Messages.get("pin-gui-title"),
            guiTitleLogin = Messages.get("gui-title-login"),
            guiTitleRegister = Messages.get("gui-title-register"),
            invalidPassword = Messages.get("password-invalid-gui");
    public static final ItemStack[] digits = getDigits();
    public static final ItemStack BARRIER = rename(new ItemStack(Material.BARRIER), "§f");
    public static final int[] EMPTY_DIGIT_SLOTS = new int[]{13, 14, 15, 16};
    public static final int FIRST_EMPTY_DIGIT_SLOT = EMPTY_DIGIT_SLOTS[0];
    public static final int[] PIN_DIGIT_SLOTS = new int[]{28, 0, 1, 2, 9, 10, 11, 18, 19, 20};
    public static final int //slots, an array in the future maybe? - Yes.
/*
            DIGIT_0 = 28,
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
    public static final Inventory gui = createGUI();

    public static PasswordBuilder newBuilder(UnverifiedUser user, BuilderType type) {
        PasswordBuilder builder = create(user, type);
        user.setGUIInitialized(true);
        return builder;
    }

    private static PasswordBuilder create(UnverifiedUser user, BuilderType type) {
        switch (type) {
            case PIN:
                return new SimplePinBuilder(user);
            case ANVIL:
                return new AnvilPasswordBuilder(user);
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

    private static Inventory createNew() {
        return Bukkit.createInventory(null, 36, pinGUITitle);
    }

    public static Inventory getPINCloned() {
        Inventory cloned = createNew();
        cloned.setContents(gui.getContents());
        return cloned;
    }

    private static Inventory createGUI() {
        Inventory inv = createNew();
        for (byte i = 0; i < 10; i++) {
            inv.setItem(PIN_DIGIT_SLOTS[i], digits[i]);
        }
/*        inv.setItem(DIGIT_0, digits[0]);
        inv.setItem(DIGIT_1, digits[1]);
        inv.setItem(DIGIT_2, digits[2]);
        inv.setItem(DIGIT_3, digits[3]);
        inv.setItem(DIGIT_4, digits[4]);
        inv.setItem(DIGIT_5, digits[5]);
        inv.setItem(DIGIT_6, digits[6]);
        inv.setItem(DIGIT_7, digits[7]);
        inv.setItem(DIGIT_8, digits[8]);
        inv.setItem(DIGIT_9, digits[9]);*/

        inv.setItem(ACTION_PIN_CONFIRM, rename(new ItemStack(Material.GREEN_WOOL), pinConfirm));
        inv.setItem(ACTION_LAST_REMOVE, rename(new ItemStack(Material.YELLOW_WOOL), pinRemoveLast));
        inv.setItem(ACTION_RESET, rename(new ItemStack(Material.RED_WOOL), pinReset));
        inv.setItem(ACTION_LEAVE, rename(new ItemStack(Material.BLACK_WOOL), pinLeave));

        for (int i : EMPTY_DIGIT_SLOTS) {
            inv.setItem(i, BARRIER);
        }

        ItemStack glassPane = rename(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), "§f");
        for (byte i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir()) inv.setItem(i, glassPane);
        }
        return inv;
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

        boolean skull = itemType.equals("wooden_skull");

        if (!validMaterial) {
            if (!skull) {
                Main.logWarning("Invalid argument at pin-digit-item-type in the config - The given parameter " + itemType
                        + " is neither a built-in version nor a valid material! The built-in version 'wooden_skull' will be used instead, as default.");
            }
            skull = true;
        }

        for (byte i = 0; i < 10; i++) {
            ItemStack item = skull ? ofDigit(i) : new ItemStack(m);
            if (i == 0) item.setAmount(itemAmountZero);
            else item.setAmount(digitEquivalent ? i : itemAmount);
            a[i] = item;
        }
        return a;
    }

    private static ItemStack rename(ItemStack i, String s) {
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(s);
        i.setItemMeta(meta);
        return i;
    }

    private static ItemStack ofDigit(byte digit) {
        String encodedHead = encodedWoodenDigitSkullProperty(digit);
        return AlixUtils.getSkull(encodedHead, "§e" + digit);
    }

    private static String encodedChatStoneSkullProperty(byte digit) {
        switch (digit) {
            case 0:
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmI4N2I0NDExMWE5ZjMzOWVkNzAxNWQwZjJjYjY0NmNlNmI4YzU5YTBiNzUwYjI3MjQ0OWFlYWMyNTYyNWRmYSJ9fX0=";
            case 1:
                return "";
        }
        return null;
    }

    private static String encodedWoodenDigitSkullProperty(byte digit) {
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
        return null;
    }
}