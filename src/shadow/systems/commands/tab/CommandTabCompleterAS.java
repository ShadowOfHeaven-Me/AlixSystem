package shadow.systems.commands.tab;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import alix.common.antibot.connection.filters.ServerPingManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CommandTabCompleterAS implements TabCompleter {

    private static final List<String> jsCommands = getASCommands();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return args.length == 1 ? jsCommands : null;
    }

    private static List<String> getASCommands() {
        List<String> list = Arrays.asList("user","resetpassword", "valueof", "constants", "info", "calculate", "average", "incognitooff", "randommath", "help");// : Arrays.asList("gracz", "wartosc", "stale", "oblicz", "info", "srednia", "incognitooff", "losowerownanie", "pomoc");
        list = new ArrayList<>(list);
        if (ServerPingManager.isRegistered()) list.add("pings");
        return list;
    }
}