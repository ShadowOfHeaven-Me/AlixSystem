package alix.common.utils;

import alix.common.CommonAlixMain;
import alix.common.environment.ServerEnvironment;
import alix.common.scheduler.impl.InterfaceAlixScheduler;
import alix.common.scheduler.impl.bukkit.BukkitAlixScheduler;
import alix.common.scheduler.impl.bukkit.PaperAlixScheduler;
import alix.common.scheduler.impl.proxy.VelocityAlixScheduler;

public class AlixCommonHandler {

    public static InterfaceAlixScheduler createSchedulerImpl() {
        switch (ServerEnvironment.getEnvironment()) {
            case PAPER:
                CommonAlixMain.logInfo("Using the optimized PaperAlixScheduler for task execution.");
                return new PaperAlixScheduler();
            case VELOCITY:
                CommonAlixMain.logInfo("Using VelocityAlixScheduler for task execution.");
                return new VelocityAlixScheduler();
            case SPIGOT:
                CommonAlixMain.logInfo("Using the unoptimized BukkitAlixScheduler for task execution - Paper is suggested for better performance.");
                return new BukkitAlixScheduler();
        }
        throw new InternalError("Invalid: " + ServerEnvironment.getEnvironment());
    }
}