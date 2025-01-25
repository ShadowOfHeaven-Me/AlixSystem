package shadow.systems.commands.tab.subtypes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class WarpCommandTabCompleter implements TabCompleter {

    private static final List<String> warps = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return args.length == 1 ? warps : null;
    }

    public static void add(String warp) {
        warps.add(warp);
    }

    public static void remove(String warp) {
        warps.remove(warp);
    }

/*    public static void replace(String oldName, String newName) {
        for (int i = 0; i < warps.size(); i++) {
            if(warps.get(i).equals(oldName)) {
                warps.set(i, newName);
                break;
            }
        }
    }*/
}