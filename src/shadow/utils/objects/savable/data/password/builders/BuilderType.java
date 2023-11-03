package shadow.utils.objects.savable.data.password.builders;

import shadow.utils.objects.savable.data.password.PasswordType;

public enum BuilderType {

    PIN(PasswordType.PIN),
    ANVIL(PasswordType.PASSWORD);

    private final PasswordType type;

    BuilderType(PasswordType type) {
        this.type = type;
    }

    public PasswordType toPasswordType() {
        return type;
    }
}