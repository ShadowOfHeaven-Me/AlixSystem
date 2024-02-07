package alix.common.data;

import alix.common.AlixCommonMain;

public enum LoginType {

    COMMAND,
    PIN,
    ANVIL;

    public static LoginType parseData(String config) {
        switch (config) {
            case "ANVIL_PASSWORD":
            case "ANVIL":
                return ANVIL;
            case "PIN":
                return PIN;
            default:
                return COMMAND;
        }
    }

    public static LoginType parseConfig(String config) {
        switch (config) {
            case "PASSWORD":
            case "COMMAND":
                return COMMAND;
            case "ANVIL_PASSWORD":
            case "ANVIL":
                return ANVIL;
            case "PIN":
                return PIN;
            default:
                AlixCommonMain.logWarning("Invalid login type in config: '" + config + "'! Defaulting to 'COMMAND'!");
                return COMMAND;
        }
    }
}