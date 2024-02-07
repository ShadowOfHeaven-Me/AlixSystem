package alix.common.messages;

import alix.common.utils.formatter.AlixFormatter;

public final class AlixMessage {

    private final String message;

    AlixMessage(String message) {
        this.message = message;
    }

    public final String format(Object... args) {
        return AlixFormatter.format(this.message, args);
    }
}