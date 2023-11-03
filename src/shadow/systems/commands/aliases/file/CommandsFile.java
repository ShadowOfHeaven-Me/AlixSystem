package shadow.systems.commands.aliases.file;

import shadow.Main;
import shadow.systems.commands.aliases.AlixCommand;
import shadow.utils.main.file.FileManager;

import java.io.File;
import java.util.*;

public class CommandsFile extends FileManager {

    private final Set<String> loginCommands = new HashSet<>();//Set for fast #contains
    private final List<String> commandNames = new ArrayList<>();
    private final List<AlixCommand> commands = new ArrayList<>();
    //private final SpecializedList<AlixCommand.Builder> list = new SpecializedList<>();

    public CommandsFile() {
        super(findFile());
    }

    @Override
    protected void loadLine(String line) {
        String[] a = line.split(": ");
        String cmd = a[0];
        String aliasLine = a[1];
        String[] aliases = aliasLine.equals("-") ? null : aliasLine.split(", ");
        switch (cmd) {
            case "captcha":
            case "register":
            case "login":
                loginCommands.add(cmd);
                if (aliases != null) loginCommands.addAll(Arrays.asList(aliases));
        }

        commandNames.add(cmd);
        commands.add(new AlixCommand(cmd, aliases));
    }

    public final Set<String> getLoginCommands() {
        return loginCommands;
    }

    public final List<String> getCommandNames() {
        return commandNames;
    }

    public final List<AlixCommand> getCommands() {
        return commands;
    }

    private static File findFile() {
        File f = FileManager.getPluginFile("commands.txt");
        if (f.exists()) return f;
        Main.debug("Unable to find this plugin's commands.txt file. Generating a new one.");
        return createPluginFile("commands.txt");
    }
}