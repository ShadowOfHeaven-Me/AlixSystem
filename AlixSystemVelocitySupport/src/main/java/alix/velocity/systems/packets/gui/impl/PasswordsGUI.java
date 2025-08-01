package alix.velocity.systems.packets.gui.impl;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.messages.AlixMessage;
import alix.common.packets.inventory.AlixInventoryType;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.other.throwable.AlixError;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.changes.DataChanges;
import alix.velocity.systems.packets.gui.inv.InventoryGui;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ua.nanit.limbo.connection.login.packets.SoundPackets;
import ua.nanit.limbo.connection.login.gui.LimboAuthBuilder;

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

    private final AlixGUI originalGui;

    private PasswordsGUI(VerifiedUser user, AlixGUI originalGui) {
        super(user, AlixInventoryType.GENERIC_9X2, guiTitle);
        this.originalGui = originalGui;
    }

    @Override
    protected GUIItem[] create(InventoryGui inv) {
        PersistentUserData data = inv.getData();
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
            switch (event.getButton()) {
                case 0://LEFT
                    LoginTypeItem c = loginTypeItemList.next();
                    changes.setLoginType(c.loginType);
                    gui.setItem(0, c.item);
                    break;
                case 1://RIGHT
                    LoginTypeItem c2 = loginTypeItemList.previous();
                    changes.setLoginType(c2.loginType);
                    gui.setItem(0, c2.item);
                    break;
            }
        });

        ItemStack i2 = extraLoginTypeItem0.item;
        items[1] = new GUIItem(i2, event -> {
            switch (event.getButton()) {
                case 0://LEFT
                    LoginTypeItem c = extraLoginTypeItemList.next();
                    changes.setExtraLoginType(c.loginType);
                    gui.setItem(1, c.item);
                    break;
                case 1://RIGHT
                    LoginTypeItem c2 = extraLoginTypeItemList.previous();
                    changes.setExtraLoginType(c2.loginType);
                    gui.setItem(1, c2.item);
                    break;
            }
        });

        ItemStack i3 = INPUT_PASSWORD.copy();
        items[9] = new GUIItem(i3, event -> user.getDuplexProcessor().enablePasswordSetting(password -> {
            user.getDuplexProcessor().disablePasswordSetting();
            changes.setPassword(password);
            gui.setItem(9, setLore(enchant(i3), mainPasswordChange.format(password)));
            this.map(); //set this gui as used
        }, () -> {
            user.getDuplexProcessor().disablePasswordSetting();
            this.map();//set this gui as used
        }, changes::getLoginType));

        ItemStack i4 = INPUT_SECONDARY_PASSWORD.copy();
        items[10] = new GUIItem(i4, event -> user.getDuplexProcessor().enablePasswordSetting(password -> {
            user.getDuplexProcessor().disablePasswordSetting();
            changes.setExtraPassword(password);
            gui.setItem(10, setLore(enchant(i4), secondaryPasswordChange.format(password)));
            this.map();//set this gui as used
        }, () -> {
            user.getDuplexProcessor().disablePasswordSetting();
            this.map();//set this gui as used
        }, changes::getExtraLoginType));

        items[8] = new GUIItem(GO_BACK_ITEM, event -> {
            this.originalGui.map();//set the original gui as used
        });

        ItemStack i5 = SAVE_CHANGES;
        items[17] = new GUIItem(i5, event -> {
            if (changes.tryApply(this.user.user)) {
                this.user.write(SoundPackets.PLAYER_LEVELUP);
                user.user.sendMessage(appliedChanges);
                user.closeInventory();
            } else this.user.writeAndFlush(SoundPackets.VILLAGER_NO);//the tryApply method will provide the text feedback
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

    public static void add(VerifiedUser user, AlixGUI originalGui) {
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

/*    public static void remove(Player player) {
        MAP.remove(player.getUniqueId());
    }*/
}