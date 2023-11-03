package shadow.systems.commands.tab.subtypes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class ChatCommandTabCompleter implements TabCompleter {

    private static final List<String> commands = Arrays.asList("on", "off", "clear");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return args.length == 1 ? commands : null;
    }
}