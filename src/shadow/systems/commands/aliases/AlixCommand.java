package shadow.systems.commands.aliases;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AlixCommand {

    private final String command;
    private final String[] aliases;

    public AlixCommand(String command, String[] aliases) {
        this.command = command;
        this.aliases = aliases;
    }

    public String getCommand() {
        return command;
    }

    public String[] getAliases() {
        return aliases;
    }

    public List<String> createAliasesList() {
        return Arrays.asList(aliases);
    }

    public boolean hasAliases() {
        return aliases != null;
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