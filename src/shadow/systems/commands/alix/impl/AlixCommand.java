package shadow.systems.commands.alix.impl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import shadow.systems.commands.alix.AlixCommandInfo;

import java.util.Collections;
import java.util.List;

public final class AlixCommand extends Command {

    private static final List<String> NO_SUGGESTIONS = Collections.emptyList();
    private final CommandExecutor executor;
    private final TabCompleter completer;

    public AlixCommand(AlixCommandInfo info, String permission, CommandExecutor executor, TabCompleter completer) {
        super(info.getCommand(), "An AlixSystem Custom Command ", "/" + info.getCommand(), info.createAliasesList());
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
        return this.completer != null ? unnullify(this.completer.onTabComplete(sender, this, alias, args)) : NO_SUGGESTIONS;
    }

    private static List<String> unnullify(List<String> list) {
        return list != null ? list : NO_SUGGESTIONS;
    }
}