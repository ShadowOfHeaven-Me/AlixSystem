package alix.common.antibot.epoll.syn;

public enum SynOption {
    MSS("mss"),
    NOP("nop"),
    WS("ws"),
    SACK_PERMITTED("sok"),
    TIMESTAMP("ts"),
    EOL("eol"),
    UNKNOWN("?");

    private final String token;

    SynOption(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    public static SynOption fromTcpKind(int kind) {
        return switch (kind) {
            case 2 -> MSS;
            case 3 -> WS;
            case 4 -> SACK_PERMITTED;
            case 8 -> TIMESTAMP;
            case 0 -> EOL;
            case 1 -> NOP;
            default -> UNKNOWN;
        };
    }
}