package alix.common.data.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Setting<T> {

    static int cnt;
    static final List<Setting<?>> SETTINGS = new ArrayList<>();
    public static final Setting<Boolean> VERIFIED_EMAIL = new Setting<>(Boolean::parseBoolean);

    private final Function<String, T> parser;
    private final Function<T, String> formatter;
    private final int ordinal;

    Setting(Function<String, T> parser) {
        this(parser, T::toString);
    }

    Setting(Function<String, T> parser, Function<T, String> formatter) {
        this.parser = parser;
        this.formatter = formatter;
        this.ordinal = cnt++;

        SETTINGS.add(this);
    }

    T parse(String line) {
        return this.parser.apply(line);
    }

    String format(T o) {
        return this.formatter.apply(o);
    }

    int ordinal() {
        return this.ordinal;
    }

    public static <T> Setting<T> of(int ordinal) {
        return (Setting<T>) SETTINGS.get(ordinal);
    }

    public static int count() {
        return cnt;
    }
}