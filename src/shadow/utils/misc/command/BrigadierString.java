package shadow.utils.misc.command;

import java.util.Collections;
import java.util.List;

//https://wiki.vg/Command_Data#brigadier:string
public final class BrigadierString {

    public static final List<Object> SINGLE_WORD = property(0);
    public static final List<Object> QUOTABLE_PHRASE = property(1);
    public static final List<Object> GREEDY_PHRASE = property(2);

    private static List<Object> property(int id) {
        return Collections.singletonList(id);
    }
}