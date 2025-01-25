package shadow.systems.gui.impl;

import alix.common.data.LoginType;
import alix.common.messages.AlixMessage;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.other.throwable.AlixError;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.systems.gui.AlixGUI;
import shadow.systems.gui.item.GUIItem;
import shadow.utils.misc.version.AlixMaterials;
import shadow.utils.objects.savable.data.DataChanges;
import alix.common.data.PersistentUserData;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.users.types.VerifiedUser;
import shadow.utils.users.UserManager;

import java.util.Arrays;

import static alix.common.messages.Messages.*;

public final class PasswordsGUI extends AlixGUI {

    private static final String
            guiTitle = get("gui-title-passwords"),
            appliedChanges = getWithPrefix("gui-passwords-applied-changes");

    private static final AlixMessage
            mainPasswordChange = getAsObject("gui-passwords-changed-main"),
            secondaryPasswordChange = getAsObject("gui-passwords-changed-secondary");

    private static final LoginTypeItem
            COMMAND_LOGIN_TYPE = new LoginTypeItem(create(AlixMaterials.COMMAND_BLOCK.getItemCloned(), get("gui-passwords-login-type-command")), LoginType.COMMAND),
            PIN_LOGIN_TYPE = new LoginTypeItem(rename(PasswordGui.digits[1].clone(), get("gui-passwords-login-type-pin")), LoginType.PIN),
            ANVIL_LOGIN_TYPE = new LoginTypeItem(create(Material.ANVIL, get("gui-passwords-login-type-anvil")), LoginType.ANVIL);

    private static final LoginTypeItem
            NO_LOGIN_TYPE_SECONDARY = new LoginTypeItem(create(Material.BARRIER, get("gui-passwords-login-type-secondary-disabled")), null),
            COMMAND_LOGIN_TYPE_SECONDARY = new LoginTypeItem(create(AlixMaterials.COMMAND_BLOCK.getItemCloned(), get("gui-passwords-login-type-secondary-command")), LoginType.COMMAND),
            PIN_LOGIN_TYPE_SECONDARY = new LoginTypeItem(rename(PasswordGui.digits[1].clone(), get("gui-passwords-login-type-secondary-pin")), LoginType.PIN),
            ANVIL_LOGIN_TYPE_SECONDARY = new LoginTypeItem(create(Material.ANVIL, get("gui-passwords-login-type-secondary-anvil")), LoginType.ANVIL);

    private static final ItemStack
            SAVE_CHANGES = create(AlixMaterials.GREEN_CONCRETE.getItemCloned(), get("gui-passwords-save-changes")),
            INPUT_PASSWORD = create(AlixMaterials.OAK_SIGN.getItemCloned(), get("gui-passwords-change-main")),
            INPUT_SECONDARY_PASSWORD = create(AlixMaterials.OAK_SIGN.getItemCloned(), get("gui-passwords-change-secondary"));
    //ENCHANTED_INPUTTED_PASSWORD = create(Material.OAK_SIGN, "&aChange Password");

/*    static {
        ENCHANTED_INPUTTED_PASSWORD.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 10);
        ItemMeta meta = ENCHANTED_INPUTTED_PASSWORD.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        ENCHANTED_INPUTTED_PASSWORD.setItemMeta(meta);
    }*/

    private static final LoginTypeItem[] LOGIN_TYPE_ARRAY = new LoginTypeItem[]{
            COMMAND_LOGIN_TYPE, PIN_LOGIN_TYPE, ANVIL_LOGIN_TYPE
    };

    private static final LoginTypeItem[] EXTRA_LOGIN_TYPE_ARRAY = new LoginTypeItem[]{
            NO_LOGIN_TYPE_SECONDARY, COMMAND_LOGIN_TYPE_SECONDARY, PIN_LOGIN_TYPE_SECONDARY, ANVIL_LOGIN_TYPE_SECONDARY
    };

    private final AbstractAlixGUI originalGui;

    private PasswordsGUI(Player player, AbstractAlixGUI originalGui) {
        super(Bukkit.createInventory(player, 18, guiTitle), player);
        this.originalGui = originalGui;
    }

    @Override
    protected GUIItem[] create(Player player) {
        VerifiedUser user = UserManager.getVerifiedUser(player);

        PersistentUserData data = user.getData();
        DataChanges changes = new DataChanges(data);

        LoopList<LoginTypeItem> loginTypeItemList = LoopList.of(LOGIN_TYPE_ARRAY);
        LoopList<LoginTypeItem> extraLoginTypeItemList = LoopList.of(EXTRA_LOGIN_TYPE_ARRAY);

        LoginTypeItem loginTypeItem0 = getItemFromLoginType(data.getLoginType(), false);
        LoginTypeItem extraLoginTypeItem0 = getItemFromLoginType(data.getLoginParams().getExtraLoginType(), true);

        loginTypeItemList.setCurrentIndex(loginTypeItemList.indexOf(loginTypeItem0));
        extraLoginTypeItemList.setCurrentIndex(extraLoginTypeItemList.indexOf(extraLoginTypeItem0));

        GUIItem[] items = new GUIItem[18];
        Arrays.fill(items, BACKGROUND_ITEM);

        ItemStack i1 = loginTypeItem0.item;
        items[0] = new GUIItem(i1, event -> {
            switch (event.getClick()) {
                case LEFT:
                    LoginTypeItem c = loginTypeItemList.next();
                    changes.setLoginType(c.loginType);
                    gui.setItem(0, c.item);
                    break;
                case RIGHT:
                    LoginTypeItem c2 = loginTypeItemList.previous();
                    changes.setLoginType(c2.loginType);
                    gui.setItem(0, c2.item);
                    break;
            }
        });

        ItemStack i2 = extraLoginTypeItem0.item;
        items[1] = new GUIItem(i2, event -> {
            switch (event.getClick()) {
                case LEFT:
                    LoginTypeItem c = extraLoginTypeItemList.next();
                    changes.setExtraLoginType(c.loginType);
                    gui.setItem(1, c.item);
                    break;
                case RIGHT:
                    LoginTypeItem c2 = extraLoginTypeItemList.previous();
                    changes.setExtraLoginType(c2.loginType);
                    gui.setItem(1, c2.item);
                    break;
            }
        });

        ItemStack i3 = INPUT_PASSWORD.clone();
        items[9] = new GUIItem(i3, event -> user.getDuplexProcessor().enablePasswordSetting(password -> {
            user.getDuplexProcessor().disablePasswordSetting();
            MAP.put(user.getUUID(), this);//set this gui as used
            user.getPlayer().openInventory(this.gui);
            changes.setPassword(password);
            gui.setItem(9, setLore(enchant(i3), mainPasswordChange.format(password)));
        }, () -> {
            user.getDuplexProcessor().disablePasswordSetting();
            MAP.put(user.getUUID(), this);//set this gui as used
            user.getPlayer().openInventory(this.gui);
        }, changes::getLoginType));

        ItemStack i4 = INPUT_SECONDARY_PASSWORD.clone();
        items[10] = new GUIItem(i4, event -> user.getDuplexProcessor().enablePasswordSetting(password -> {
            user.getDuplexProcessor().disablePasswordSetting();
            MAP.put(user.getUUID(), this);//set this gui as used
            user.getPlayer().openInventory(this.gui);
            changes.setExtraPassword(password);
            gui.setItem(10, setLore(enchant(i4), secondaryPasswordChange.format(password)));
        }, () -> {
            user.getDuplexProcessor().disablePasswordSetting();
            MAP.put(user.getUUID(), this);//set this gui as used
            user.getPlayer().openInventory(this.gui);
        }, changes::getExtraLoginType));

        items[8] = new GUIItem(GO_BACK_ITEM, event -> {
            MAP.put(user.getUUID(), this.originalGui);//set the original gui as used
            user.getPlayer().openInventory(this.originalGui.getGUI());
        });

        ItemStack i5 = SAVE_CHANGES;
        items[17] = new GUIItem(i5, event -> {
            if (changes.tryApply(player)) {
                player.sendRawMessage(appliedChanges);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                player.closeInventory();
            } else
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);//the tryApply method will provide the text feedback
        });

        return items;
    }

    private static LoginTypeItem getItemFromLoginType(LoginType type, boolean extra) {
        if (type == null) {
            if (!extra) throw new AlixError();
            return NO_LOGIN_TYPE_SECONDARY;
        }
        switch (type) {
            case COMMAND:
                return extra ? COMMAND_LOGIN_TYPE_SECONDARY : COMMAND_LOGIN_TYPE;
            case PIN:
                return extra ? PIN_LOGIN_TYPE_SECONDARY : PIN_LOGIN_TYPE;
            case ANVIL:
                return extra ? ANVIL_LOGIN_TYPE_SECONDARY : ANVIL_LOGIN_TYPE;
            default:
                throw new AlixError("Invalid type: " + type);
        }
    }

    public static void add(Player player, AbstractAlixGUI originalGui) {
        AlixScheduler.async(() -> {
            PasswordsGUI gui = new PasswordsGUI(player, originalGui);
            AlixScheduler.sync(() -> {
                MAP.put(player.getUniqueId(), gui);
                player.openInventory(gui.gui);
            });
        });
    }

    private static final class LoginTypeItem {

        private final ItemStack item;
        private final LoginType loginType;

        private LoginTypeItem(ItemStack item, LoginType loginType) {
            this.item = item;
            this.loginType = loginType;
        }
    }

/*    public static void remove(Player player) {
        MAP.remove(player.getUniqueId());
    }*/
}