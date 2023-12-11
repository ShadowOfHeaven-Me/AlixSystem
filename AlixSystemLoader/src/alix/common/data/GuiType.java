package alix.common.data;

public enum GuiType {

    PIN(PasswordType.PIN),
    ANVIL(PasswordType.PASSWORD);

    private final PasswordType type;

    GuiType(PasswordType type) {
        this.type = type;
    }

    public PasswordType toPasswordType() {
        return type;
    }
}