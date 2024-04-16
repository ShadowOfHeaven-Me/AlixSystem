package shadow.systems.gui.impl;

import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import shadow.Main;
import shadow.systems.gui.AlixGUI;
import shadow.systems.gui.item.GUIItem;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class IpAutoLoginGUI extends AlixGUI {

    private static final IpAutoLoginGUI INSTANCE = new IpAutoLoginGUI();
    private final String
            messageAccept = Messages.getWithPrefix("ip-autologin-accept"),
            messageReject = Messages.getWithPrefix("ip-autologin-reject");

    private IpAutoLoginGUI() {
        super(Bukkit.createInventory(null, InventoryType.DROPPER, Messages.get("gui-title-ip-autologin")));
    }

    @Override
    protected GUIItem[] create(Player unusedNull) {
        GUIItem[] items = new GUIItem[9];
        Arrays.fill(items, BACKGROUND_ITEM);

        ItemStack questionMark = AlixUtils.getSkull(Messages.get("gui-ip-autologin-question-mark"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
        String[] lore = Messages.get("gui-ip-autologin-what-is-this").split("-nl ");
        items[1] = new GUIItem(setLore(questionMark, lore));

        String[] lore2 = Messages.get("gui-ip-autologin-lore-accept").split("-nl ");
        ItemStack confirm = create(Material.GREEN_WOOL, Messages.get("gui-ip-autologin-accept"), lore2);
        items[6] = new GUIItem(setLore(confirm, lore2), event -> {
            Player player = (Player) event.getWhoClicked();
            PersistentUserData data = UserFileManager.get(player.getName());
            data.getLoginParams().setIpAutoLogin(true);
            player.sendMessage(this.messageAccept);
            AlixScheduler.runLaterSync(player::closeInventory, 100, TimeUnit.MILLISECONDS);//we need to delay it because of a ConcurrentModificationException in the 'MAP' in AlixGUI
        });

        String[] lore3 = Messages.get("gui-ip-autologin-lore-reject").split("-nl ");
        ItemStack reject = create(Material.RED_WOOL, Messages.get("gui-ip-autologin-reject"), lore2);
        items[8] = new GUIItem(setLore(reject, lore3), event -> {
            Player player = (Player) event.getWhoClicked();
            PersistentUserData data = UserFileManager.get(player.getName());
            data.getLoginParams().setIpAutoLogin(false);
            player.sendMessage(this.messageReject);
            AlixScheduler.runLaterSync(player::closeInventory, 100, TimeUnit.MILLISECONDS);//we need to delay it because of a ConcurrentModificationException in the 'MAP' in AlixGUI
        });

        return items;
    }

    public static void add(Player player) {
        if (Thread.currentThread() == Main.mainServerThread) add0(player);
        else AlixScheduler.sync(() -> add0(player));
    }

    private static void add0(Player player) {
        MAP.put(player.getUniqueId(), INSTANCE);
        player.openInventory(INSTANCE.gui);
    }

/*    public static void remove(Player player) {
        MAP.remove(player.getUniqueId());
    }*/

    public static void init() {
    }
}