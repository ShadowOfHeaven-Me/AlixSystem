package shadow.systems.gui.impl;

import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import shadow.systems.gui.AbstractAlixGUI;
import shadow.systems.gui.AlixGUI;
import shadow.systems.gui.item.GUIItem;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.users.types.VerifiedUser;
import shadow.utils.users.UserManager;

import java.util.Arrays;
import java.util.function.Function;

public final class LoginSettingsGUI extends AlixGUI {

    private static final String loginSettingsTitle = Messages.get("gui-title-login-settings");
    private static final ItemStack
            IP_AUTOLOGIN_ON = create(Material.GREEN_CONCRETE, Messages.get("gui-login-settings-ip-autologin-on")),
            IP_AUTOLOGIN_OFF = create(Material.RED_CONCRETE, Messages.get("gui-login-settings-ip-autologin-off"));
    private static final Function<PersistentUserData, ItemStack> IP_AUTOLOGIN_GET = data -> data.getLoginParams().getIpAutoLogin() ? IP_AUTOLOGIN_ON : IP_AUTOLOGIN_OFF;

    private final AbstractAlixGUI originalGui;

    private LoginSettingsGUI(Player player, AbstractAlixGUI originalGui) {
        super(Bukkit.createInventory(player, 9, loginSettingsTitle), player);
        this.originalGui = originalGui;
    }

    @Override
    protected GUIItem[] create(Player player) {
        GUIItem[] items = new GUIItem[9];
        Arrays.fill(items, BACKGROUND_ITEM);

        VerifiedUser user = UserManager.getVerifiedUser(player);
        PersistentUserData data = user.getData();

        ItemStack i1 = IP_AUTOLOGIN_GET.apply(data);
        items[0] = new GUIItem(i1, event -> {
            data.getLoginParams().setIpAutoLogin(!data.getLoginParams().getIpAutoLogin());
            gui.setItem(0, IP_AUTOLOGIN_GET.apply(data));
        });

        items[8] = new GUIItem(GO_BACK_ITEM, event -> {
            MAP.put(user.getUUID(), this.originalGui);//set this gui as used
            user.getPlayer().openInventory(this.originalGui.getGUI());
        });


        return items;
    }

    public static void add(Player player, AbstractAlixGUI originalGui) {
        AlixScheduler.async(() -> {
            LoginSettingsGUI gui = new LoginSettingsGUI(player, originalGui);
            AlixScheduler.sync(() -> {
                MAP.put(player.getUniqueId(), gui);
                player.openInventory(gui.gui);
            });
        });
    }

/*    public static void remove(Player player) {
        MAP.remove(player.getUniqueId());
    }*/
}