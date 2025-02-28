package alix.common.commands.file;

import alix.common.utils.file.AlixFileManager;

import java.io.File;
import java.util.*;

public final class CommandsFile extends AlixFileManager {

    private final Set<String> loginCommands = new HashSet<>();//Set for fast #contains
    private final Map<String, AlixCommandInfo> alixCommands = new HashMap<>();
    //private final SpecializedList<AlixCommand.Builder> list = new SpecializedList<>();

    public CommandsFile() {
        super(findFile());
    }

    @Override
    protected void loadLine(String line) {
        String[] a = line.replaceAll(" ", "").split(":");

        //Main.logInfo("Line: " + line + " array: " + Arrays.toString(a));

        String cmd = a[0];
        boolean registered = cmd.charAt(0) != '#';
        if (!registered) cmd = cmd.substring(1);

        boolean fallbackRegistered = !cmd.isEmpty() && cmd.charAt(0) != '#';
        if (!fallbackRegistered) cmd = cmd.substring(1);

        String aliasLine = a[1];
        String[] aliases = aliasLine.equals("-") ? null : aliasLine.split(",");
        switch (cmd) {
            //case "captcha":
            case "register":
            case "login":
                this.loginCommands.add(cmd);
                if (aliases != null) this.loginCommands.addAll(Arrays.asList(aliases));
        }

        AlixCommandInfo alix = new AlixCommandInfo(cmd, aliases, registered, fallbackRegistered);
        this.alixCommands.put(cmd, alix);

        if (aliases != null)
            for (String alias : aliases) this.alixCommands.put(alias, alix);
    }

    public Map<String, AlixCommandInfo> getAlixCommands() {
        return alixCommands;
    }

    public Set<String> getLoginCommands() {
        return loginCommands;
    }

    private static File findFile() {
        File f = AlixFileManager.getPluginFile("commands.txt", FileType.CONFIG);
        if (f.exists()) return f;
        //Main.logDebug("Unable to find this plugin's commands.txt file. Generating a new one.");
        return createPluginFile("commands.txt", FileType.CONFIG);
    }
}