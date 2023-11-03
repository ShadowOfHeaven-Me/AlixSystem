package alix.common.utils;

import alix.common.CommonAlixMain;
import alix.common.scheduler.impl.InterfaceAlixScheduler;
import alix.common.scheduler.impl.bukkit.BukkitAlixScheduler;
import alix.common.scheduler.impl.bukkit.PaperAlixScheduler;
import alix.common.scheduler.impl.proxy.VelocityAlixScheduler;

public class AlixCommonHandler {

    public static InterfaceAlixScheduler createSchedulerImpl() {
        if (AlixCommonUtils.isValidClass("com.destroystokyo.paper.PaperConfig")) {
            CommonAlixMain.logInfo("Using the optimized PaperAlixScheduler for task execution.");
            return new PaperAlixScheduler();
        }
        if (AlixCommonUtils.isValidClass("com.velocitypowered.proxy.VelocityServer")) {
            CommonAlixMain.logInfo("Using VelocityAlixScheduler for task execution.");
            return new VelocityAlixScheduler();
        }
        CommonAlixMain.logInfo("Using the unoptimized BukkitAlixScheduler for task execution - Paper is suggested for better performance.");
        return new BukkitAlixScheduler();
    }
}