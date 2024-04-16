package shadow.utils.command.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shadow.Main;
import alix.common.messages.Messages;
import alix.common.messages.AlixMessage;
import shadow.utils.users.types.VerifiedUser;

public final class ChatManager {

    private static final AlixMessage
            chatClearFeedbackMessage = Messages.getAsObject("chat-clear"),
            chatMessageDelay = Messages.getAsObject("chat-message-delay");
    private static final String
            chatIsOff = Messages.get("chat-is-off"), mutedSelf  = Messages.get("muted-self");
    private static final String chatClearingMessage;
    private static final long delay;
    private static final boolean delayCheck;
    private static boolean isChatTurnedOn;

    //TODO: Make two different perms for delay and off chat bypass

    public static boolean cannotChat(VerifiedUser u) {
        if (u.canBypassChatStatus()) return false;
        if (!isChatTurnedOn) {
            u.sendMessage(chatIsOff);
            return true;
        }
        long now = System.currentTimeMillis();

        if (u.wasMutedAt(now)) {//equivalent of User#isMuted, but invoked like this to use the 'now' variable
            u.sendMessage(mutedSelf);
            return true;
        }

        if (delayCheck) {
            long nextPossibleChatTime = u.getNextPossibleChatTime();

            if (nextPossibleChatTime > now) {
                float tillCanChat = (nextPossibleChatTime - now) / 1000f;
                u.sendMessage(chatMessageDelay.format(tillCanChat));
                return true;
            }
            u.setNextPossibleChatTime(now + delay);
        }
        return false;
    }

    public static boolean isChatTurnedOn() {
        return isChatTurnedOn;
    }

    public static void setChatTurnedOn(boolean on, CommandSender sender) {
        if (!on) clearChat(sender);
        isChatTurnedOn = on;
    }

    public static void clearChat(CommandSender sender) {
        String messageForAll = chatClearingMessage + chatClearFeedbackMessage.format(sender.getName());
        for (Player p : Bukkit.getOnlinePlayers()) p.sendRawMessage(messageForAll);
    }

    static {
        StringBuilder sb = new StringBuilder(25000);
        for (int i = 0; i < sb.capacity(); i++) sb.append(' ');
        chatClearingMessage = sb.toString() + '\n';
        delay = Main.config.getLong("chat-delay");
        delayCheck = delay > 0;
        isChatTurnedOn = true;
    }
}