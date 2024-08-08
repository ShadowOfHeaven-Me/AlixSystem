/*
package shadow.utils.holders.offline;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class OfflineMapHolder {

    private static final Map<String, OfflinePlayer> map;

    static {
        map = new HashMap<>();
        for(OfflinePlayer o : Bukkit.getOfflinePlayers()) {
            String name = o.getName();
            if(name != null) map.put(name, o);
        }
    }

    public static void add(Player p, String name) {
        map.put(name, p);
    }
}*/
