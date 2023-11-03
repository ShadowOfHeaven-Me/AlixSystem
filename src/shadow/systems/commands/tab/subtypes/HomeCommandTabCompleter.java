package shadow.systems.commands.tab.subtypes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import shadow.utils.objects.savable.loc.NamedLocation;
import shadow.utils.users.User;
import shadow.utils.users.UserManager;

import java.util.ArrayList;
import java.util.List;

public class HomeCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1 || !(sender instanceof Player)) return null;

        Player p = (Player) sender;
        //if (Verifications.has(p)) return null;
        //User u = UserManager.getVerifiedUser(p);
        User u = UserManager.getNullableUserOnline(p);

        if (u == null) return null;

        NamedLocation[] homes = u.getHomes().asArray();
        List<String> list = new ArrayList<>(homes.length);

        for (NamedLocation home : homes) list.add(home.getName());

        return list;
    }
}