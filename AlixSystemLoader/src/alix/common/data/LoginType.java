package alix.common.data;

import alix.common.AlixCommonMain;

public enum LoginType {

    COMMAND,
    PIN,
    ANVIL;

    public static LoginType from(String t, boolean config) {
        switch (t) {
            case "PASSWORD":
            case "COMMAND":
                return COMMAND;
            case "ANVIL_PASSWORD":
            case "ANVIL":
                return ANVIL;
            case "PIN":
                return PIN;
            default:
                if (config)
                    AlixCommonMain.logWarning("Invalid login type in config: '" + config + "'! Defaulting to 'COMMAND'!");
                return COMMAND;
        }
    }
}