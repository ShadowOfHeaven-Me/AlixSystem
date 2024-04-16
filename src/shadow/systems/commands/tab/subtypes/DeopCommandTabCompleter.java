package shadow.systems.commands.tab.subtypes;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class DeopCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 1) return null;
        List<String> list = new ArrayList<>();
        for (OfflinePlayer p : Bukkit.getOperators()) {
            String n = p.getName();
            if (n != null) list.add(n);
        }
        return list;
    }
}