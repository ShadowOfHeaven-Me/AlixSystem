package alix.velocity.systems.packets.gui.impl;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.messages.AlixMessage;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.other.throwable.AlixError;
import alix.velocity.systems.packets.gui.AbstractAlixGUI;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.changes.DataChanges;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.systems.packets.gui.menu.MenuBuilder;
import alix.velocity.systems.packets.gui.menu.MenuConfig;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import ua.nanit.limbo.connection.login.gui.LimboAuthBuilder;
import ua.nanit.limbo.connection.login.packets.SoundPackets;

import java.util.Map;

import static alix.common.messages.Messages.*;

public final class PasswordsGUI extends AlixGUI {

    private static final String MENU_NAME = "passwords";

    private static final String appliedChanges = getWithPrefix("gui-passwords-applied-changes");

    private static final AlixMessage
            mainPasswordChange = getAsObject("gui-passwords-changed-main"),
            secondaryPasswordChange = getAsObject("gui-passwords-changed-secondary");

    private static final LoginTypeItem
            COMMAND_LOGIN_TYPE = new LoginTypeItem(create(ItemTypes.COMMAND_BLOCK, get("gui-passwords-login-type-command")), LoginType.COMMAND),
            PIN_LOGIN_TYPE = new LoginTypeItem(rename(LimboAuthBuilder.DIGITS[1], get("gui-passwords-login-type-pin")), LoginType.PIN),
            ANVIL_LOGIN_TYPE = new LoginTypeItem(create(ItemTypes.ANVIL, get("gui-passwords-login-type-anvil")), LoginType.ANVIL);

    private static final LoginTypeItem
            NO_LOGIN_TYPE_SECONDARY = new LoginTypeItem(create(ItemTypes.BARRIER, get("gui-passwords-login-type-secondary-disabled")), null),
            COMMAND_LOGIN_TYPE_SECONDARY = new LoginTypeItem(create(ItemTypes.COMMAND_BLOCK, get("gui-passwords-login-type-secondary-command")), LoginType.COMMAND),
            PIN_LOGIN_TYPE_SECONDARY = new LoginTypeItem(rename(LimboAuthBuilder.DIGITS[1], get("gui-passwords-login-type-secondary-pin")), LoginType.PIN),
            ANVIL_LOGIN_TYPE_SECONDARY = new LoginTypeItem(create(ItemTypes.ANVIL, get("gui-passwords-login-type-secondary-anvil")), LoginType.ANVIL);

    private static final ItemStack
            SAVE_CHANGES = create(ItemTypes.GREEN_CONCRETE, get("gui-passwords-save-changes")),
            INPUT_PASSWORD = create(ItemTypes.OAK_SIGN, get("gui-passwords-change-main")),
            INPUT_SECONDARY_PASSWORD = create(ItemTypes.OAK_SIGN, get("gui-passwords-change-secondary"));

    private static final LoginTypeItem[] LOGIN_TYPE_ARRAY = new LoginTypeItem[]{
            COMMAND_LOGIN_TYPE, PIN_LOGIN_TYPE, ANVIL_LOGIN_TYPE
    };

    private static final LoginTypeItem[] EXTRA_LOGIN_TYPE_ARRAY = new LoginTypeItem[]{
            NO_LOGIN_TYPE_SECONDARY, COMMAND_LOGIN_TYPE_SECONDARY, PIN_LOGIN_TYPE_SECONDARY, ANVIL_LOGIN_TYPE_SECONDARY
    };

    private final AbstractAlixGUI originalGui;

    private PasswordsGUI(VerifiedUser user, AbstractAlixGUI originalGui) {
        super(user, AlixInventoryType.GENERIC_9X2, MenuConfig.get(MENU_NAME).getTitle());
        this.originalGui = originalGui;
    }

    @Override
    protected GUIItem[] create(InventoryGui inv) {
        MenuConfig menu = MenuConfig.get(MENU_NAME);
        int size = AlixInventoryType.GENERIC_9X2.size();

        PersistentUserData data = inv.getData();
        DataChanges changes = new DataChanges(data);

        LoopList<LoginTypeItem> loginTypeItemList = LoopList.of(LOGIN_TYPE_ARRAY);
        LoopList<LoginTypeItem> extraLoginTypeItemList = LoopList.of(EXTRA_LOGIN_TYPE_ARRAY);

        LoginTypeItem loginTypeItem0 = getItemFromLoginType(data.getLoginType(), false);
        LoginTypeItem extraLoginTypeItem0 = getItemFromLoginType(data.getLoginParams().getExtraLoginType(), true);

        loginTypeItemList.setCurrentIndex(loginTypeItemList.indexOf(loginTypeItem0));
        extraLoginTypeItemList.setCurrentIndex(extraLoginTypeItemList.indexOf(extraLoginTypeItem0));

        int[] loginTypeSlots = menu.getSlotsForInternal("login-type");
        GUIItem loginTypeGuiItem = new GUIItem(loginTypeItem0.item, event -> {
            switch (event.getButton()) {
                case 0://LEFT
                    LoginTypeItem c = loginTypeItemList.next();
                    changes.setLoginType(c.loginType);
                    for (int slot : loginTypeSlots) gui.setItem(slot, c.item);
                    break;
                case 1://RIGHT
                    LoginTypeItem c2 = loginTypeItemList.previous();
                    changes.setLoginType(c2.loginType);
                    for (int slot : loginTypeSlots) gui.setItem(slot, c2.item);
                    break;
            }
        });

        int[] extraLoginTypeSlots = menu.getSlotsForInternal("extra-login-type");
        GUIItem extraLoginTypeGuiItem = new GUIItem(extraLoginTypeItem0.item, event -> {
            switch (event.getButton()) {
                case 0://LEFT
                    LoginTypeItem c = extraLoginTypeItemList.next();
                    changes.setExtraLoginType(c.loginType);
                    for (int slot : extraLoginTypeSlots) gui.setItem(slot, c.item);
                    break;
                case 1://RIGHT
                    LoginTypeItem c2 = extraLoginTypeItemList.previous();
                    changes.setExtraLoginType(c2.loginType);
                    for (int slot : extraLoginTypeSlots) gui.setItem(slot, c2.item);
                    break;
            }
        });

        ItemStack i3 = INPUT_PASSWORD.copy();
        int[] inputPasswordSlots = menu.getSlotsForInternal("input-password");
        GUIItem inputPasswordGuiItem = new GUIItem(i3, event -> user.getDuplexProcessor().enablePasswordSetting(password -> {
            user.getDuplexProcessor().disablePasswordSetting();
            changes.setPassword(password);
            ItemStack updated = setLore(enchant(i3), mainPasswordChange.format(password));
            for (int slot : inputPasswordSlots) gui.setItem(slot, updated);
            this.map(); //set this gui as used
        }, () -> {
            user.getDuplexProcessor().disablePasswordSetting();
            this.map();//set this gui as used
        }, changes::getLoginType));

        ItemStack i4 = INPUT_SECONDARY_PASSWORD.copy();
        int[] inputSecondaryPasswordSlots = menu.getSlotsForInternal("input-secondary-password");
        GUIItem inputSecondaryPasswordGuiItem = new GUIItem(i4, event -> user.getDuplexProcessor().enablePasswordSetting(password -> {
            user.getDuplexProcessor().disablePasswordSetting();
            changes.setExtraPassword(password);
            ItemStack updated = setLore(enchant(i4), secondaryPasswordChange.format(password));
            for (int slot : inputSecondaryPasswordSlots) gui.setItem(slot, updated);
            this.map();//set this gui as used
        }, () -> {
            user.getDuplexProcessor().disablePasswordSetting();
            this.map();//set this gui as used
        }, changes::getExtraLoginType));

        GUIItem backGuiItem = new GUIItem(GO_BACK_ITEM, event -> this.originalGui.map());//set the original gui as used

        GUIItem saveChangesGuiItem = new GUIItem(SAVE_CHANGES, event -> changes.tryApply(this.user.user, success -> {
            if (success) {
                this.user.writePacketSilently(SoundPackets.wrapperOf(Sounds.ENTITY_PLAYER_LEVELUP));
                user.user.sendMessage(appliedChanges);
                user.closeInventory();
            } else this.user.sendPacketSilently(SoundPackets.wrapperOf(Sounds.ENTITY_VILLAGER_NO));//the tryApply method will provide the text feedback
        }));

        Map<String, GUIItem> internalItems = Map.of(
                "login-type", loginTypeGuiItem,
                "extra-login-type", extraLoginTypeGuiItem,
                "input-password", inputPasswordGuiItem,
                "input-secondary-password", inputSecondaryPasswordGuiItem,
                "back", backGuiItem,
                "save-changes", saveChangesGuiItem
        );

        return MenuBuilder.build(menu, size, this.user, internalItems);
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

    public static void add(VerifiedUser user, AbstractAlixGUI originalGui) {
        AlixScheduler.async(() -> new PasswordsGUI(user, originalGui).map());
    }

    private static final class LoginTypeItem {

        private final ItemStack item;
        private final LoginType loginType;

        private LoginTypeItem(ItemStack item, LoginType loginType) {
            this.item = item;
            this.loginType = loginType;
        }
    }
}
