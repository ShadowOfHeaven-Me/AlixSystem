package alix.common.messages.types;

import alix.common.utils.formatter.AlixFormatter;

public class AlixMessage {

    private final String message;

    public AlixMessage(String message) {
        this.message = message;
    }

/*    public void send(Player p, Object... args) {
        p.sendMessage(format(args));
    }*/

    public String format(Object... args) {
        return AlixFormatter.format(message, args);
    }
}