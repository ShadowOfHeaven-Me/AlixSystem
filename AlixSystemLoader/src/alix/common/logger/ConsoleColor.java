package alix.common.logger;

public enum ConsoleColor {

    BRIGHT_RED(AlixLoggerProvider.BRIGHT_RED),
    BRIGHT_CYAN(AlixLoggerProvider.BRIGHT_CYAN);

    private final String color;

    ConsoleColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color;
    }
}