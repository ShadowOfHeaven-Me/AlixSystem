package alix.velocity.utils.data;

import alix.common.data.security.password.Password;

public final class PersistentUserData {

    private final String name;
    private final Password password;

    private PersistentUserData(String name, String password) {
        this.name = name;
        this.password = Password.fromUnhashed(password);
    }

    private PersistentUserData(String[] data) {
        this.name = data[0];
        this.password = Password.readFromSaved(data[1]);
    }

    @Override
    public String toString() {
        return name + "|" + password.toSavable();
    }

    public static PersistentUserData createDefault(String name, String password) {
        return new PersistentUserData(name, password);
    }

    public static PersistentUserData fromData(String[] data) {
        return new PersistentUserData(data);
    }
}