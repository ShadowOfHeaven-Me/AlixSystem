package shadow.systems.commands.aliases;

import shadow.systems.commands.aliases.file.CommandsFile;

public class FileCommandManager {

    private static final CommandsFile commandsFile = new CommandsFile();
    //private static final PluginYamlFile pluginYamlFile;

    public static boolean isPluginCommandPresent(String cmd) {
        return commandsFile.getCommandNames().contains(cmd);
    }

    public static AlixCommand getCommand(String cmd) {
        int index = commandsFile.getCommandNames().indexOf(cmd);
        return index != -1 ? commandsFile.getCommands().get(index) : null;
    }

    public static boolean isLoginCommand(String cmd) {
        return commandsFile.getLoginCommands().contains(cmd);
    }

    public static void initialize() {
        commandsFile.read();
/*        pluginYamlFile.read();
        pluginYamlFile.write();*/
    }

    public static CommandsFile getFileInstance() {
        return commandsFile;
    }

    /*    static {
        commandsFile = new CommandsFile();
        //pluginYamlFile = new PluginYamlFile();
    }*/
}