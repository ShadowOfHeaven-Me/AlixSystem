package shadow.systems.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class ExecutableCommandList {

    private final ExecutableCommand[] cmds;

    public ExecutableCommandList(List<String> list) {
        this.cmds = new ExecutableCommand[list.size()];
        for (int i = 0; i < cmds.length; i++) this.cmds[i] = new ExecutableCommand(list.get(i));
    }

    public void invoke(Player player) {
        for (ExecutableCommand c : cmds) c.invoke(player);
    }

    private static final class ExecutableCommand {

        private final String command;
        private final boolean executeWithPlayerAsSender, nameReplaceable, executable;

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
            this.executable = !cmdLine.isEmpty();
        }

        private void invoke(Player player) {
            if (!executable) return;
            String cmd = this.nameReplaceable ? this.command.replaceAll("%name%", player.getName()) : this.command;
            CommandSender sender = this.executeWithPlayerAsSender ? player : Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(sender, cmd);
        }
    }
}