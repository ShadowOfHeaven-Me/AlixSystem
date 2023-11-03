package shadow.systems.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import shadow.systems.dependencies.luckperms.LuckPermsExecutors;

public class Dependencies {

    public static void initAdditional() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.isPluginEnabled("LuckPerms")) LuckPermsExecutors.register();
    }
}