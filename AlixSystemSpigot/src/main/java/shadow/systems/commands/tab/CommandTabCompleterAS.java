package shadow.systems.commands.tab;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumStatus;
import alix.common.database.migrate.MigrateType;
import alix.common.utils.other.annotation.OptimizationCandidate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CommandTabCompleterAS implements TabCompleter {

    private final List<String> cmds = this.getASCommands();

    //with async complete
    @OptimizationCandidate
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
                    case "migrate": {
                        return Arrays.stream(MigrateType.values()).map(Enum::name).toList();
                    }
                    case "rs":
                    case "resetstatus":
                    case "frd":
                    case "fullyremovedata":
                    case "rp":
                    case "resetpassword":
                    case "user":
                        List<String> l = new ArrayList<>(UserFileManager.getAllData().size());
                        for (PersistentUserData d : UserFileManager.getAllData()) l.add(d.getName());
                        return l;
                }
                break;
            case 3:
                switch (args[0].toLowerCase()) {
                        /*List<String> list = new ArrayList<>(Bukkit.getOnlinePlayers().size());
                        for (Player p : Bukkit.getOnlinePlayers()) list.add(p.getName());
                        return list;*/
                    case "migrate": {
                        String arg3 = args[2].toUpperCase();
                        return Arrays.stream(MigrateType.values()).map(Enum::name).filter(n -> n.startsWith(arg3)).toList();
                    }
                    case "fs":
                    case "forcestatus":
                    case "rs":
                    case "resetstatus":
                    case "frd":
                    case "fullyremovedata":
                    case "rp":
                    case "resetpassword":
                    case "user":
                        String arg2 = args[1];
                        int capacity = Math.max(UserFileManager.getAllData().size() >> 3, 5);
                        List<String> l = new ArrayList<>(capacity);
                        for (PersistentUserData d : UserFileManager.getAllData()) {
                            String name = d.getName();
                            if (name.startsWith(arg2)) l.add(name);
                        }
                        return l;
                }

            case 4:
                switch (args[0].toLowerCase()) {
                    case "fs":
                    case "forcestatus":
                        String arg4 = args[3].toUpperCase();
                        return Arrays.stream(PremiumStatus.values()).map(Enum::name).filter(n -> n.startsWith(arg4)).toList();
                }
        }
        return null;
    }

    private List<String> getASCommands() {
        List<String> list = Arrays.asList("resetstatus","rs",
                "user", "abstats", "rp", "resetpassword", "valueof", "constants", "frd", "fullyremovedata",
                "info", "calculate", "average", "randommath", "help", "helpmath", "bypasslimit", "bypasslimit-remove", "bl", "bl-r");// : Arrays.asList("gracz", "wartosc", "stale", "oblicz", "info", "srednia", "incognitooff", "losowerownanie", "pomoc");
        list = new ArrayList<>(list);
        //if (ServerPingManager.isRegistered()) list.add("pings");
        Collections.sort(list);
        return Arrays.asList(list.toArray(new String[0]));
    }
}