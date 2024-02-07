package alix.common.messages;

import alix.common.AlixCommonMain;
import alix.common.messages.file.MessagesFile;
import alix.common.messages.file.extracted.ExtractedMessages;
import alix.common.utils.formatter.AlixFormatter;

import java.io.IOException;

public final class Messages {

    private static final MessagesFile file = new MessagesFile();
    private static ExtractedMessages extractedMessages;
    public static final String unregisteredUserMessage, notLoggedInUserMessage, captchaNotCompletedUserMessage, autoLoginMessage, chatAlreadyOn, chatAlreadyOff;

    static {
        /*try {
            file.load();
        } catch (IOException e) {
            Main.logError("File " + e.getMessage() + " has thrown an error whilst loading.");
            e.getCause().printStackTrace();
        }*/
        file.read();
        unregisteredUserMessage = getWithPrefix("unregistered-reminder");
        notLoggedInUserMessage = getWithPrefix("not-logged-in-reminder");
        captchaNotCompletedUserMessage = getWithPrefix("uncompleted-captcha-reminder");
        autoLoginMessage = getWithPrefix("auto-login");
        chatAlreadyOn = getWithPrefix("chat-already-on");
        chatAlreadyOff = getWithPrefix("chat-already-off");
        extractedMessages = ExtractedMessages.findFile();
    }

    public static AlixMessage getAsObject(String s, Object... toFormatWith) {
        return new AlixMessage(get(s, toFormatWith));
    }

    public static String getWithPrefix(String s, Object... toFormatWith) {
        return AlixFormatter.appendPrefix(AlixFormatter.format(get(s), toFormatWith));
    }

    public static String get(String s) {
        String m = file.getMap().get(s);
        if (m == null)
            AlixCommonMain.logWarning("Message '" + s + "' was not found in the messages.txt file! Report this immediately!");
        return m != null ? m : "&c<Message not found>!";
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
        extractedMessages.load();
        try {
            file.save0(extractedMessages.getFormattedMessages());
        } catch (IOException e) {
            throw new RuntimeException(e);
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