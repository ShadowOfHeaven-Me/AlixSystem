package alix.common.commands.file;

import java.util.Arrays;
import java.util.Set;

public final class CommandsFileManager {

    private static final CommandsFile commandsFile = new CommandsFile();

/*    public static boolean isPluginCommandPresent(String cmd) {
        return commandsFile.getCommandNames().contains(cmd);
    }*/

    public static AlixCommandInfo getCommand(String cmd) {
        return commandsFile.getAlixCommands().get(cmd);
    }

    public static String[] getAliases(String cmd) {
        return getCommand(cmd).getAliasesNotNull();
    }

/*    public static boolean isLoginCommand(String cmd) {
        return commandsFile.getLoginCommands().contains(cmd);
    }*/

    private static final String fallbackPrefixText = "alixsystem:";
    private static final char[] fallbackPrefix = fallbackPrefixText.toCharArray();

    public static char[] removeFallbackPrefix(char[] cmd) {
        for (int i = 0; i < fallbackPrefix.length; i++)
            if (cmd[i] != fallbackPrefix[i]) return cmd;
        return Arrays.copyOfRange(cmd, fallbackPrefix.length, cmd.length);
    }

    public static String removeFallbackPrefix(String cmd) {
        return cmd.startsWith(fallbackPrefixText) ? cmd.substring(fallbackPrefix.length) : cmd;
    }

    public static boolean isPasswordChangeCommand(String s) {
        AlixCommandInfo cmd = getCommand(removeFallbackPrefix(s));
        return cmd != null && cmd.getCommand().equals("changepassword");
    }

    public static char[] getLowerCasedUnslashedCommand(String a) {
        char[] b = new char[a.length() - 1];//the chars without the '/'
        a.getChars(1, a.length(), b, 0);//exclude the slash in the copying

        for (int c = 0; c < b.length; c++) {
            char d = b[c];
            if (d == ' ') break;//only lowercase the command, not the arguments
            if (d >= 65 && d <= 90) d += 32;
            b[c] = d;
        }

        return b;
    }

    static {
        commandsFile.loadExceptionless();
    }

    public static void init() {
    }

    /**
     * v1.5.2 ("/as reload"): re-reads commands.txt in place - safe to call again as-is (loadLine()
     * above only ever does Map#put()/Set#add(), both overwrite-safe), UNLIKE ServerSettings#reload()
     * or AlixYamlConfigFile#reload(), which needed extra care first.
     * <p>
     * IMPORTANT LIMITATION: this refreshes what getCommand()/isPasswordChangeCommand()/etc. return, but
     * NOT which command names/aliases are actually registered with the platform's command dispatcher
     * (Velocity's Brigadier tree is built once, at plugin startup, from whatever commands.txt said back
     * then - see AlixSystemCommand#register()). Adding/renaming an alias in commands.txt still needs a
     * real proxy restart to take effect; toggling an existing command's "#" (registered) prefix does
     * too, for the same reason. Only settings this file stores that are actually re-read AFTER startup
     * (e.g. isPasswordChangeCommand() checks) benefit from a reload without a restart.
     */
    public static void reload() {
        commandsFile.loadExceptionless();
    }

    private CommandsFileManager() {
    }

    public static Set<String> getLoginCommands() {
        return commandsFile.getLoginCommands();
    }
}