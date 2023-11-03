/*
package shadow.utils.main.file.types;

import org.bukkit.configuration.file.YamlConfiguration;
import shadow.Main;
import shadow.systems.commands.aliases.AlixCommand;
import shadow.systems.commands.aliases.CommandAliasManager;
import shadow.utils.main.file.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginYamlFile extends FileManager {

    //private final SpecializedList<AlixCommandBuilder> commands;
    private final List<String> lines;
    private String currentCommand;
    private int index = -1, lastCommandIndex;
    private boolean commandsPrefix, skip;

    public PluginYamlFile() {
        super(FileManager.getWithJarCompiledFile("plugin.yml"));
        lines = new ArrayList<>();
        Main.logInfo(new File(Main.instance.getDataFolder(), "plugin.yml").exists() + " ");
        //commands = new SpecializedList<>();
    }

    @Override
    protected void loadLine(String line) {
        index++;
        if (!commandsPrefix) {
            lines.add(line);
            return;
        }
        if (line.startsWith("commands:")) {
            commandsPrefix = true;
            lines.add(line);
            return;
        }
        if (line.startsWith("  ") && !line.startsWith("    ")) {//it's a command prefix
            if(currentCommand != null) {//a command with no aliases previously, now has aliases
                //lines.add(lastCommandIndex + 1,
            }
            currentCommand = line.substring(2);
            lastCommandIndex = index;
        }
        //lines.add(1,
        if (line.startsWith("    aliases: [ ")) {

            AlixCommand cmd = CommandAliasManager.getCommand(currentCommand);

            currentCommand = null;

            if (cmd == null || cmd.getAliases() == null) return;
            lines.add(createWholeAliasLine(cmd.getAliases()));
            //commands.last().aliases = getAliases(line);
        }
    }

    public void write() {
        for(String s : lines) {
            Main.logInfo(s);
        }
        try {
            save(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createWholeAliasLine(String[] aliases) {
        StringBuilder sb = new StringBuilder("    aliases: [ ");
        for (String s : aliases) {
            sb.append(s).append(',');
        }
        return sb.substring(sb.length() - 1) + " ]";
    }

*/
/*    private static String[] getAliases(String wholeAliasLine) {
        String prefix = "    aliases: [ ";
        String aliasesLine = wholeAliasLine.substring(prefix.length(), wholeAliasLine.length() - 2);
        return aliasesLine.split(", ");
    }*//*


*/
/*    public static class AlixCommandBuilder {

        public String command;
        public String[] aliases;

        public AlixCommandBuilder setCommand(String command) {
            this.command = command;
            return this;
        }
    }*//*

}*/
