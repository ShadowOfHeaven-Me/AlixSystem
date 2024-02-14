package shadow.systems.commands.tab;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CommandTabCompleterAS implements TabCompleter {

    private final List<String> cmds = this.getASCommands();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return args.length == 1 ? cmds : null;
    }

    private List<String> getASCommands() {
        List<String> list = Arrays.asList("user","abstats","resetpassword", "valueof", "constants", "info", "calculate", "average", "incognitooff", "randommath", "help");// : Arrays.asList("gracz", "wartosc", "stale", "oblicz", "info", "srednia", "incognitooff", "losowerownanie", "pomoc");
        list = new ArrayList<>(list);
        //if (ServerPingManager.isRegistered()) list.add("pings");
        Collections.sort(list);
        return list;
    }
}