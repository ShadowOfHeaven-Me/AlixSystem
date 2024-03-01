package shadow.systems.commands.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CommandTabCompleterAS implements TabCompleter {

    private final List<String> cmds = this.getASCommands();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        switch (args.length) {
            case 1:
                return cmds;
            case 2:
                switch (args[0].toLowerCase()) {
                        /*List<String> list = new ArrayList<>(Bukkit.getOnlinePlayers().size());
                        for (Player p : Bukkit.getOnlinePlayers()) list.add(p.getName());
                        return list;*/
                    case "rp":
                    case "resetpassword":
                    case "user":
                        List<String> l = new ArrayList<>(UserFileManager.getAllData().size());
                        for (PersistentUserData d : UserFileManager.getAllData()) l.add(d.getName());
                        return l;
                }
        }
        return null;
    }

    private List<String> getASCommands() {
        List<String> list = Arrays.asList("user", "abstats", "rp", "resetpassword", "valueof", "constants", "info", "calculate", "average", "randommath", "help", "helpmath");// : Arrays.asList("gracz", "wartosc", "stale", "oblicz", "info", "srednia", "incognitooff", "losowerownanie", "pomoc");
        list = new ArrayList<>(list);
        //if (ServerPingManager.isRegistered()) list.add("pings");
        Collections.sort(list);
        return list;
    }
}