/*
package shadow.systems.executors.gui;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import shadow.utils.users.Verifications;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.password.builders.BuilderType;
import shadow.utils.objects.savable.data.password.builders.PasswordBuilder;
import shadow.utils.objects.savable.data.password.builders.PasswordGui;
import shadow.utils.users.offline.UnverifiedUser;

public class AnvilGuiExecutors implements Listener {

    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {
        //AsyncScheduler
        Bukkit.broadcastMessage("333333333333");

        if (!(event.getView().getPlayer() instanceof Player)) return;

        Bukkit.broadcastMessage("22222222222222");

        Player player = (Player) event.getView().getPlayer();

        UnverifiedUser user = Verifications.get(player);
        if (user == null || !user.isGuiUser() || !user.isGUIInitialized()) return;

        Bukkit.broadcastMessage("000000000");

        PasswordBuilder builder = user.getPasswordBuilder();
        if (builder.getType() != BuilderType.ANVIL) return;

        ItemStack result = event.getResult();
        if (result == null) return;

        Bukkit.broadcastMessage("1111111111");
        //GUIPacketBlocker

        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        String password = event.getInventory().getRenameText();

        Bukkit.broadcastMessage(event.getInventory().getRenameText());

        if (!user.isRegistered() && AlixUtils.isPasswordInvalid(password)) {
            event.getInventory().setRepairCost(999999999);
            return;
        }

        builder.input(event.getInventory().getRenameText());

        meta.setDisplayName(PasswordGui.pinConfirm);
        result.setItemMeta(meta);
    }
}*/
