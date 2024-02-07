package shadow.systems.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shadow.Main;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixUtils;

import java.util.ArrayList;
import java.util.List;

public final class ExecutableCommandList {

    private final ExecutableCommand[] cmds;

    public ExecutableCommandList(List<String> config) {
        List<ExecutableCommand> cmds = new ArrayList<>();
        for (String cmd : config) {
            ExecutableCommand c = constructIfValid(cmd);
            if (c != null) cmds.add(c);
        }
        this.cmds = cmds.toArray(new ExecutableCommand[0]);
    }

    public final void invoke(Player player) {
        for (ExecutableCommand c : cmds) c.invoke(player);
    }

    private static ExecutableCommand constructIfValid(String cmd) {
        cmd = AlixUtils.unslashify(cmd.trim());
        /*String cmdArg = cmd.split(" ")[0];
        if (!ReflectionUtils.serverKnownCommands.containsKey(cmdArg)) {
            Main.logWarning("The given command '" + cmdArg + "' is not valid! Ignoring this command line!");
            return null;
        }*/
        ExecutableCommand c = new ExecutableCommand(cmd);
        if (c.command.isEmpty()) return null;
        return c;
    }

    private static final class ExecutableCommand {

        private final String command;
        private final boolean executeWithPlayerAsSender, nameReplaceable;

        private ExecutableCommand(String cmd) {
            String cmdLine = cmd.trim();
            boolean playerExecute;
            if (cmdLine.endsWith("-p")) {
                playerExecute = true;
                cmdLine = cmdLine.substring(0, cmdLine.length() - 2).trim();
            } else playerExecute = false;
            this.command = cmdLine;
            this.nameReplaceable = cmdLine.contains("%name%");
            this.executeWithPlayerAsSender = playerExecute;
        }

        private void invoke(Player player) {
            String cmd = this.nameReplaceable ? this.command.replaceAll("%name%", player.getName()) : this.command;
            CommandSender sender = this.executeWithPlayerAsSender ? player : Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(sender, cmd);
        }
    }
}