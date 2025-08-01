/*
package alix.velocity.systems.packets.gui.impl;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.collections.list.LoopList;
import alix.velocity.systems.packets.gui.AlixGUI;
import alix.velocity.systems.packets.gui.changes.DataChanges;
import alix.velocity.systems.packets.gui.GUIItem;
import alix.velocity.systems.packets.gui.inv.InventoryGui;

import java.util.Arrays;

public final class GoogleAuthGUI extends AlixGUI {

    private static final GUIItem whatIsThis, GO_BACK_GUI_ITEM;
    private static final ItemStack showQRCodeItem, applyChangesItem;
    private static final AuthItemType PASSWORD, AUTH, AUTH_AND_PASSWORD;
    private static final String guiTitle;

    static {
        guiTitle = Messages.get("gui-title-google-auth");
        PASSWORD = new AuthItemType(setLore(create(ItemTypes.OBSIDIAN, Messages.get("gui-google-auth-config-password-name")), Messages.getSplit("gui-google-auth-config-password-lore")), AuthSetting.PASSWORD);
        AUTH = new AuthItemType(setLore(create(ItemTypes.NETHER_STAR, Messages.get("gui-google-auth-config-auth-name")), Messages.getSplit("gui-google-auth-config-auth-lore")), AuthSetting.AUTH_APP);
        AUTH_AND_PASSWORD = new AuthItemType(setLore(create(ItemTypes.BEACON, Messages.get("gui-google-auth-config-auth-and-password-name")), Messages.getSplit("gui-google-auth-config-auth-and-password-lore")), AuthSetting.PASSWORD_AND_AUTH_APP);
    }

    private static final AuthItemType[] AUTH_TYPES = {
            PASSWORD, AUTH, AUTH_AND_PASSWORD
    };

    static {
        DataChanges
        GO_BACK_GUI_ITEM = new GUIItem(GO_BACK_ITEM, e -> AccountGUI.add((Player) e.getWhoClicked()));

        whatIsThis = new GUIItem(AlixUtils.getSkull(Messages.get("gui-google-auth-what-is-this-name"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMyNzEwNTI3MTllZjY0MDc5ZWU4YzE0OTg5NTEyMzhhNzRkYWM0YzI3Yjk1NjQwZGI2ZmJkZGMyZDZiNWI2ZSJ9fX0="));
        String[] loreWhatIsThis = Messages.get("gui-google-auth-what-is-this").split(" -nl ");
        setLore(whatIsThis.getItem(), loreWhatIsThis);

        showQRCodeItem = AlixUtils.getSkull(Messages.get("gui-google-auth-show-qr-code-name"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTUzYzE0OTUwZmMzNjQ2NzhiNzU1NDRhY2IxZGEwYzk0MjBiNTA2ZTU4NzEyMDM5M2IzZDFhZDQ4OThlNzRmIn19fQ==");
        String[] loreQRCode = Messages.get("gui-google-auth-show-qr-code-lore").split(" -nl ");
        setLore(showQRCodeItem, loreQRCode);

        applyChangesItem = rename(ItemTypes.GREEN_CONCRETE, Messages.get("gui-google-auth-apply-changes"));
    }

    //private final VerifiedUser user;

    private GoogleAuthGUI(Player player) {
        super(Bukkit.createInventory(player, 27, guiTitle), player);
        //this.user = UserManager.getVerifiedUser(player);
    }

    @Override
    protected GUIItem[] create(InventoryGui inv) {
        GUIItem[] items = new GUIItem[27];
        Arrays.fill(items, BACKGROUND_ITEM);
        AuthDataChanges changes = new AuthDataChanges();

        LoginParams params = user.getData().getLoginParams();

        LoopList<AuthItemType> authList = LoopList.of(AUTH_TYPES);
        authList.setCurrentIndex(authList.indexOfFirst(t -> t.authSetting.equals(params.getAuthSettings())));

        items[8] = GO_BACK_GUI_ITEM;

        items[10] = whatIsThis;

        items[13] = new GUIItem(showQRCodeItem, e -> {
            AlixScheduler.async(() -> GoogleAuth.showQRCode(user, inv));
            //the inv is closed here /\
            MAP.remove(inv.getUniqueId());
        });

        items[16] = new GUIItem(authList.current().item, event -> {
            ClickType type = event.getClick();
            switch (type) {
                case LEFT:
                case RIGHT:
                    AuthItemType c = type == ClickType.RIGHT ? authList.previous() : authList.next();
                    changes.setAuthSetting(c.authSetting);
                    gui.setItem(16, c.item);
                    break;
            }
        });

        items[26] = new GUIItem(applyChangesItem, event -> changes.tryApply(user));
        return items;
    }

    public static void add(Player player) {
        GoogleAuthGUI gui = new GoogleAuthGUI(player);
        MAP.put(player.getUniqueId(), gui);
        player.openInventory(gui.gui);
    }

    private static final class AuthItemType {

        private final ItemStack item;
        private final AuthSetting authSetting;

        private AuthItemType(ItemStack item, AuthSetting authSetting) {
            this.item = item;
            this.authSetting = authSetting;
        }
    }
}*/
