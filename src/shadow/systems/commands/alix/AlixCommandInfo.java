package shadow.systems.commands.alix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AlixCommandInfo {

    private final String command;
    private final String[] aliases;
    private final boolean registered;

    public AlixCommandInfo(String command, String[] aliases, boolean registered) {
        this.command = command;
        this.aliases = aliases;
        this.registered = registered;
    }

    public String getCommand() {
        return command;
    }

    public String[] getAliases() {
        return aliases;
    }

    public List<String> createAliasesList() {
        return this.hasAliases() ? new ArrayList<>(Arrays.asList(aliases)) : new ArrayList<>();
    }

    public boolean hasAliases() {
        return aliases != null;
    }

    public boolean isRegistered() {
        return registered;
    }

    /*    public static class Builder {
        public String command;
        public String[] aliases;

        public Builder setCommand(String command) {
            this.command = command;
            return this;
        }

        public Builder setAliases(String[] aliases) {
            this.aliases = aliases;
            return this;
        }

        public AlixCommand build() {
            return new AlixCommand(command, aliases);
        }
    }*/
}