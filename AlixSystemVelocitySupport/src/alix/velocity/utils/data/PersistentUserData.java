package alix.velocity.utils.data;

import alix.common.data.Password;

public final class PersistentUserData {

    private final String name;
    private final Password password;

    private PersistentUserData(String name, String password) {
        this.name = name;
        this.password = Password.fromUnhashed(password);
    }

    private PersistentUserData(String line) {
        String[] s = line.split("\\|");
        this.name = s[0];
        this.password = Password.readFromSaved(s[1]);
    }

    @Override
    public String toString() {
        return name + "|" + password.toSavable();
    }

    public static PersistentUserData createDefault(String name, String password) {
        return new PersistentUserData(name, password);
    }

    public static PersistentUserData fromLine(String line) {
        return new PersistentUserData(line);
    }
}