package shadow.systems.commands.alix.impl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import alix.common.commands.file.AlixCommandInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AlixCommand extends Command {

    private static final List<String> NO_SUGGESTIONS = Collections.emptyList();
    private final CommandExecutor executor;
    private final TabCompleter completer;

    public AlixCommand(AlixCommandInfo info, String permission, CommandExecutor executor, TabCompleter completer) {
        super(info.getCommand(), "An AlixSystem Command ", "/" + info.getCommand(), info.createAliasesList());
        this.executor = executor;
        this.completer = completer;
        this.setPermission(permission);
    }

    public AlixCommand(String name, String permission, CommandExecutor executor, TabCompleter completer) {
        super(name);
        this.executor = executor;
        this.completer = completer;
        this.setPermission(permission);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return this.executor.onCommand(sender, this, label, args);
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return this.completer != null ? tidyUp(this.completer.onTabComplete(sender, this, alias, args), args) : NO_SUGGESTIONS;
    }

    private static List<String> tidyUp(List<String> list, String[] args) {
        if (list == null) return NO_SUGGESTIONS;//un-nullify the list
        if (args.length != 1) return list;
        String arg1 = args[0];
        List<String> p = list.getClass() == ArrayList.class ? list : new ArrayList<>(list);//instantiate a copy if the list is not an ArrayList, since it usually holds constant elements for one, and for two is probably unmodifiable, and for three you wouldn't want to modify it even it was modifiable
        p.removeIf(s -> !s.startsWith(arg1));
        return p;
    }
}