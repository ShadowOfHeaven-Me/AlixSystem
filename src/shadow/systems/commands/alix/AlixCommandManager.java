package shadow.systems.commands.alix;

import org.bukkit.entity.Player;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.file.CommandsFile;
import shadow.systems.commands.alix.verification.VerificationCommand;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static shadow.utils.main.AlixUtils.sendMessage;

public final class AlixCommandManager {

    private static final CommandsFile commandsFile = new CommandsFile();
    private static final Map<String, VerificationCommand> verificationCommands = new HashMap<>();

/*    public static boolean isPluginCommandPresent(String cmd) {
        return commandsFile.getCommandNames().contains(cmd);
    }*/

    public static AlixCommand getCommand(String cmd) {
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
        AlixCommand cmd = getCommand(removeFallbackPrefix(s));
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

    //This is a custom verification command handling implementation
    //I've deemed to be the fastest so far
    public static void handleVerificationCommand(char[] cmd, UnverifiedUser user) {
        Player p = user.getPlayer();
        cmd = removeFallbackPrefix(cmd);
        boolean isArgSize0 = true;
        char[] arg0Chars = cmd;
        int lastIndex = 0;//the starting index point of the arguments

        for (int i = 0; i < cmd.length; i++) {
            if (cmd[i] == ' ') {//the space symbol separates the arguments
                arg0Chars = Arrays.copyOfRange(cmd, 0, i);
                lastIndex = i + 1;
                isArgSize0 = lastIndex == cmd.length;//the cmd ends with a space, so the arg count is still 0
                break;
            }
        }

        String arg0 = new String(arg0Chars);
        VerificationCommand consumer = verificationCommands.get(arg0);

        if (consumer == null) return;//the unverified user tried to execute a command different to a verification one

        if (isArgSize0) {
            if (consumer == VerificationCommand.OF_CAPTCHA) {
                sendMessage(p, CommandManager.formatCaptcha);
                return;
            }
            if (consumer == VerificationCommand.OF_LOGIN) {
                sendMessage(p, CommandManager.formatLogin);
                return;
            }
            if (consumer == VerificationCommand.OF_REGISTER) {
                sendMessage(p, CommandManager.formatRegister);
                return;
            }
            throw new AssertionError("Invalid: " + arg0 + " for " + new String(cmd));
        }

        //This line of code passes the whole string as the second command argument
        //This if fine due to checks performed later on, like AlixUtils.getInvalidityReason
        //used in the register command being aware of the space symbol being invalid
        String arg2 = new String(Arrays.copyOfRange(cmd, lastIndex, cmd.length));

        consumer.onCommand(user, p, arg2);
    }

    static {
        commandsFile.read();
        Map<String, VerificationCommand> map = verificationCommands;

        VerificationCommand captcha = VerificationCommand.OF_CAPTCHA;
        VerificationCommand register = VerificationCommand.OF_REGISTER;
        VerificationCommand login = VerificationCommand.OF_LOGIN;

        for (String commandAlias : commandsFile.getLoginCommands()) {
            AlixCommand alix = getCommand(commandAlias);
            if (alix == null)
                throw new ExceptionInInitializerError("Invalid verification command: '" + commandAlias + "'!");
            String command = alix.getCommand();
            switch (command) {
                case "captcha":
                    map.put(commandAlias, captcha);
                    continue;
                case "register":
                    map.put(commandAlias, register);
                    continue;
                case "login":
                    map.put(commandAlias, login);
                    continue;
                default:
                    throw new AssertionError("Invalid verification command: '" + commandAlias + "' - '" + command + "'!");
            }
        }
    }

    public static void init() {
    }

    private AlixCommandManager() {
    }
}