package alix.common.messages;

import alix.common.AlixCommonMain;
import alix.common.messages.file.MessagesFile;
import alix.common.messages.file.extracted.ExtractedMessages;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.other.throwable.AlixError;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public final class Messages {

    private static final MessagesFile file = new MessagesFile();
    private static ExtractedMessages extractedMessages;
    public static final String notLoggedInUserMessage, autoLoginMessage, chatAlreadyOn, chatAlreadyOff;

    static {
        file.loadExceptionless();
        notLoggedInUserMessage = get("not-logged-in-reminder");
        autoLoginMessage = getWithPrefix("auto-login");
        chatAlreadyOn = getWithPrefix("chat-already-on");
        chatAlreadyOff = getWithPrefix("chat-already-off");
        extractedMessages = ExtractedMessages.findFile();
        warnAboutMissingTranslations();
    }

    /**
     * On a platform offering multiple bundled language files (currently just Velocity's "language"
     * config option), warns once at startup if the selected language is missing any key the reference/
     * default language has - e.g. a key added by a plugin update that hasn't been translated into every
     * bundled language yet. Without this, a missing key only ever surfaces as a scattered
     * "'<Message not found: ...>'" the first time some player happens to trigger that exact message, at
     * an arbitrary point during normal server operation, instead of as one clear warning an operator
     * actually sees at startup. A no-op on a platform with only a single messages file (see
     * AlixMain.Params#referenceMessagesFileName's default).
     */
    private static void warnAboutMissingTranslations() {
        var params = AlixCommonMain.MAIN_CLASS_INSTANCE.getEngineParams();
        String activeName = params.messagesFileName();
        String referenceName = params.referenceMessagesFileName();
        if (referenceName.equals(activeName)) return;

        try (InputStream in = AlixFileManager.getBundledResourceStream(referenceName)) {
            if (in == null) return;//reference not bundled (shouldn't happen) - nothing to compare against

            Set<String> missing = new TreeSet<>();
            char separator = params.messagesSeparator();
            Pattern splitPattern = Pattern.compile(Pattern.quote(separator + " "));

            AlixFileManager.readLines(in, line -> {
                if (line.isBlank() || line.startsWith("#")) return;
                String[] a = splitPattern.split(line, 2);
                if (a.length == 1) a = line.split(Pattern.quote(String.valueOf(separator)), 2);
                if (a.length == 2 && !file.getMap().containsKey(a[0])) missing.add(a[0]);
            }, false);

            if (!missing.isEmpty())
                AlixCommonMain.logWarning("The selected language file '" + activeName + "' is missing " + missing.size()
                        + " message key(s) present in the reference '" + referenceName + "': " + missing
                        + " - each will show as '<Message not found: ...>' in-game until translated.");
        } catch (IOException e) {
            AlixCommonMain.logWarning("Could not check '" + activeName + "' for missing translation keys: " + e.getMessage());
        }
    }

    public static AlixMessage getAsObject(String s, Object... toFormatWith) {
        return new AlixMessage(get(s, toFormatWith));
    }

    public static String getWithPrefix(String s, Object... toFormatWith) {
        return AlixFormatter.appendPrefix(AlixFormatter.format(get(s), toFormatWith));
    }

    public static String[] getSplit(String s) {
        return get0(s).split(" -nl ");
    }

    public static String get(String s) {
        //String newLine = Pattern.quote("\n");
        return get0(s);//.replaceAll(newLine, "\n 0777").replaceAll(" -nl ", "\n");
    }

    private static String get0(String s) {
        String m = file.getMap().get(s);
        if (m == null)
            AlixCommonMain.logWarning("Message '" + s + "' was not found in the " + AlixCommonMain.MAIN_CLASS_INSTANCE.getEngineParams().messagesFileName() + " file! This is likely not good!");
        return m != null ? AlixFormatter.translateColors(m) : "§c<Message not found: '" + s + "'>!";
    }

    public static String get(String s, Object... toFormatWith) {
        return AlixFormatter.format(get(s), toFormatWith);
    }

    public static MessagesFile getFileInstance() {
        return file;
    }

    public static boolean extract() {
        if (extractedMessages != null) return false;
        extractedMessages = new ExtractedMessages(file);
        return true;
    }

    public static boolean merge() {
        if (extractedMessages == null) return false;
        try {
            extractedMessages.load();
            file.save0(extractedMessages.getFormattedMessages());
        } catch (IOException e) {
            throw new AlixError(e);
        }
        extractedMessages.getFile().delete();
        extractedMessages = null;
        return true;
    }

    public static void init() {
    }

    private Messages() {
    }
}