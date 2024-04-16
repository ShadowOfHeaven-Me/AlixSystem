package shadow.systems.commands.tab.subtypes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class OpCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return null;
        List<String> list = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) if (!player.isOp()) list.add(player.getName());
        return list;
    }
}