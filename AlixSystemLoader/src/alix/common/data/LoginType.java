package alix.common.data;

import alix.common.AlixCommonMain;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public enum LoginType {

    COMMAND,
    PIN,
    ANVIL,
    //Additional, not used
    AUTH_2FA;

    public static LoginType from(String t, boolean config) {
        boolean isOlderThan1_14 = PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_14);
        switch (t) {
            case "PASSWORD":
            case "COMMAND":
                return COMMAND;
            case "ANVIL_PASSWORD":
            case "ANVIL":
                if (config && isOlderThan1_14) {
                    AlixCommonMain.logWarning("Login type ANVIL is available at versions 1.14+! Defaulting to 'COMMAND'!");
                    return COMMAND;
                }
                return ANVIL;
            case "PIN":
                if (config && isOlderThan1_14) {
                    AlixCommonMain.logWarning("Login type PIN is available at versions 1.14+! Defaulting to 'COMMAND'!");
                    return COMMAND;
                }
                return PIN;
            default:
                if (config)
                    AlixCommonMain.logWarning("Invalid login type in config: '" + config + "'! Defaulting to 'COMMAND'!");
                return COMMAND;
        }
    }
}