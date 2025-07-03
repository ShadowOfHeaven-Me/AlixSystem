package alix.common.commands.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AlixCommandInfo {

    private final String command;
    private final String[] aliases;
    private final boolean registered, fallbackRegistered;

    public AlixCommandInfo(String command, String[] aliases, boolean registered, boolean fallbackRegistered) {
        this.command = command;
        this.aliases = aliases;
        this.registered = registered;
        this.fallbackRegistered = fallbackRegistered;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getLabels() {
        var list = this.createAliasesList();
        list.add(this.command);
        return list;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String[] getAliasesNotNull() {
        return this.hasAliases() ? this.aliases : new String[0];
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

    public boolean isFallbackRegistered() {
        return fallbackRegistered;
    }

    public boolean isAnyCommandRegistered() {
        return registered || fallbackRegistered;
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