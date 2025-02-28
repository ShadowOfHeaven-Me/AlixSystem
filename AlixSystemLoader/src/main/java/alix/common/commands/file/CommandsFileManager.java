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

    private CommandsFileManager() {
    }

    public static Set<String> getLoginCommands() {
        return commandsFile.getLoginCommands();
    }
}