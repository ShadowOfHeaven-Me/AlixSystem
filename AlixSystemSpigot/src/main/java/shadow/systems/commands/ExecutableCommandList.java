package shadow.systems.commands;

import alix.common.commands.executable.AbstractCommandList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class ExecutableCommandList extends AbstractCommandList<Player, ConsoleCommandSender, CommandSender> {

    public ExecutableCommandList(List<String> config) {
        super(config, Player::getName, Bukkit.getConsoleSender(), Bukkit::dispatchCommand);
    }
}