package alix.common.packets.command.arg;

import java.util.Collections;
import java.util.List;

public enum ArgumentString implements TypedArgument {

    SINGLE_WORD(0),
    QUOTABLE_PHRASE(1),
    GREEDY_PHRASE(2);

    private final List<Object> property;

    ArgumentString(int property) {
        this.property = property(0);
    }

    private static List<Object> property(int id) {
        return Collections.singletonList(id);
    }

}