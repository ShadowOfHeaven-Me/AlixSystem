package alix.common.environment;

import alix.common.utils.AlixCommonUtils;

public enum ServerEnvironment {

    SPIGOT, PAPER, VELOCITY, BUNGEE;

    private static final ServerEnvironment env = establishEnv0();

    public static ServerEnvironment getEnvironment() {
        return env;
    }

    private static ServerEnvironment establishEnv0() {
        if (AlixCommonUtils.isValidClass("com.destroystokyo.paper.PaperConfig")) return PAPER;
        if (AlixCommonUtils.isValidClass("com.velocitypowered.proxy.VelocityServer")) return VELOCITY;
        return SPIGOT;
    }
}