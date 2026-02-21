package alix.common.commands.executable;

import alix.common.utils.AlixCommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractCommandList<P extends S, C extends S, S> {

    private final ExecutableCommand<P, C, S>[] cmds;
    private final Function<P, String> getName;
    private final C console;
    private final BiConsumer<S, String> dispatchCommand;

    protected AbstractCommandList(List<String> config, Function<P, String> getName, C console, BiConsumer<S, String> dispatchCommand) {
        this.getName = getName;
        this.console = console;
        this.dispatchCommand = dispatchCommand;
        List<ExecutableCommand> cmds = new ArrayList<>();
        for (String cmd : config) {
            ExecutableCommand c = constructIfValid(cmd);
            if (c != null) cmds.add(c);
        }
        this.cmds = cmds.toArray(new ExecutableCommand[0]);
    }

    public final void invoke(P player) {
        String name = this.getName.apply(player);
        for (ExecutableCommand c : cmds) c.invoke(player, this.console, name, this.dispatchCommand);
    }

    private static ExecutableCommand constructIfValid(String cmd) {
        cmd = AlixCommonUtils.unslashify(cmd.trim()).trim();
        /*String cmdArg = cmd.split(" ")[0];
        if (!ReflectionUtils.serverKnownCommands.containsKey(cmdArg)) {
            Main.logWarning("The given command '" + cmdArg + "' is not valid! Ignoring this command line!");
            return null;
        }*/
        ExecutableCommand c = new ExecutableCommand(cmd);
        if (c.command.isEmpty()) return null;
        return c;
    }

    private static final class ExecutableCommand<P extends S, C extends S, S> {

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

        private void invoke(P player, C console, String name, BiConsumer<S, String> dispatchCommand) {
            String cmd = this.nameReplaceable ? this.command.replaceAll("%name%", name) : this.command;
            S sender = this.executeWithPlayerAsSender ? player : console;
            dispatchCommand.accept(sender, cmd);
        }
    }
}