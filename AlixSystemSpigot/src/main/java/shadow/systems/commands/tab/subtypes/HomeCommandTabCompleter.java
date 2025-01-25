package shadow.systems.commands.tab.subtypes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import alix.common.data.loc.impl.bukkit.BukkitNamedLocation;
import shadow.utils.users.types.VerifiedUser;
import shadow.utils.users.UserManager;

import java.util.ArrayList;
import java.util.List;

public final class HomeCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1 || !(sender instanceof Player)) return null;

        Player p = (Player) sender;
        //if (Verifications.has(p)) return null;
        //User u = UserManager.getVerifiedUser(p);
        VerifiedUser u = UserManager.getNullableVerifiedUser(p);

        if (u == null) return null;

        BukkitNamedLocation[] homes = u.getHomes().array();
        List<String> list = new ArrayList<>(homes.length);

        for (BukkitNamedLocation home : homes) list.add(home.getName());

        return list;
    }
}