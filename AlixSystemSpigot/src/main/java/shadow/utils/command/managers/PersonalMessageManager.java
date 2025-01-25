package shadow.utils.command.managers;

import java.util.HashMap;
import java.util.Map;

//Basically, /msg, but with a fancy name
public final class PersonalMessageManager {

    private static final Map<String, String> receiverToSender = new HashMap<>();

    public static void add(String sender, String receiver) {
        receiverToSender.put(receiver, sender);
    }

    public static String get(String receiver) {
        return receiverToSender.get(receiver);
    }
}