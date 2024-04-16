package shadow.systems.commands.tab.subtypes;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class NameBanCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return null;
        Set<OfflinePlayer> set = Bukkit.getBannedPlayers();
        List<String> list = new ArrayList<>(set.size());
        for (OfflinePlayer p : set) list.add(p.getName());
        return list;
    }
}