package shadow.systems.commands.tab.subtypes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class IPBanCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return null;
        Set<String> set = Bukkit.getIPBans();
        List<String> list = new ArrayList<>(set.size());
        list.addAll(set);
        return list;
    }
}