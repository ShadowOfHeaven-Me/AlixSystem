package shadow.systems.commands.alix.file;

import shadow.Main;
import shadow.systems.commands.alix.AlixCommand;
import shadow.utils.main.file.FileManager;

import java.io.File;
import java.util.*;

public final class CommandsFile extends FileManager {

    private final Set<String> loginCommands = new HashSet<>();//Set for fast #contains
    private final Map<String, AlixCommand> alixCommands = new HashMap<>();
    //private final SpecializedList<AlixCommand.Builder> list = new SpecializedList<>();

    public CommandsFile() {
        super(findFile());
    }

    @Override
    protected void loadLine(String line) {
        String[] a = line.replaceAll(" ", "").split(":");
        String cmd = a[0];
        String aliasLine = a[1];
        String[] aliases = aliasLine.equals("-") ? null : aliasLine.split(",");
        switch (cmd) {
            case "captcha":
            case "register":
            case "login":
                this.loginCommands.add(cmd);
                if (aliases != null) this.loginCommands.addAll(Arrays.asList(aliases));
        }

        AlixCommand alix = new AlixCommand(cmd, aliases);
        this.alixCommands.put(cmd, alix);

        if (aliases != null)
            for(String alias : aliases) this.alixCommands.put(alias, alix);
    }

    public final Map<String, AlixCommand> getAlixCommands() {
        return alixCommands;
    }

    public final Set<String> getLoginCommands() {
        return loginCommands;
    }

    private static File findFile() {
        File f = FileManager.getPluginFile("commands.txt");
        if (f.exists()) return f;
        Main.debug("Unable to find this plugin's commands.txt file. Generating a new one.");
        return createPluginFile("commands.txt");
    }
}