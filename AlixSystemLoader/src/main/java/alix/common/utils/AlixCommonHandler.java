package alix.common.utils;

import alix.common.AlixCommonMain;
import alix.common.AlixMain;
import alix.common.environment.ServerEnvironment;
import alix.common.logger.AlixLoggerProvider;
import alix.common.scheduler.impl.InterfaceAlixScheduler;
import alix.common.scheduler.impl.bukkit.BukkitAlixScheduler;
import alix.common.scheduler.impl.bukkit.PaperAlixScheduler;
import alix.common.scheduler.impl.proxy.VelocityAlixScheduler;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.config.bukkit.BukkitConfigImpl;
import alix.common.utils.config.velocity.VelocityConfigImpl;
import alix.common.utils.multiengine.ban.AbstractBanList;
import alix.common.utils.multiengine.ban.BukkitBanList;
import alix.common.utils.multiengine.server.AbstractServer;
import alix.common.utils.multiengine.server.BukkitServer;
import alix.loaders.bukkit.BukkitAlixMain;
import alix.loaders.velocity.VelocityAlixMain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static alix.common.reflection.CommonReflection.invoke;

public final class AlixCommonHandler {

    public static InterfaceAlixScheduler createSchedulerImpl() {
        switch (ServerEnvironment.getEnvironment()) {
            case PAPER:
                try {
                    Class.forName("com.destroystokyo.paper.event.server.ServerTickEndEvent");
                    AlixCommonMain.logInfo("Using the optimized PaperAlixScheduler for task execution.");
                    return new PaperAlixScheduler();
                } catch (ClassNotFoundException e) {
                    AlixCommonMain.logInfo("Using BukkitAlixScheduler for task execution due to a low Paper version.");
                    return new BukkitAlixScheduler();
                }
            case VELOCITY:
                AlixCommonMain.logInfo("Using VelocityAlixScheduler for task execution.");
                return new VelocityAlixScheduler();
            case SPIGOT:
                AlixCommonMain.logInfo("Using the unoptimized BukkitAlixScheduler for task execution - Paper is suggested for better performance.");
                return new BukkitAlixScheduler();
            default:
                throw new InternalError("Invalid: " + ServerEnvironment.getEnvironment());
        }
    }

    public static ExecutorService createExecutorForBlockingTasks() {
        try {
            ExecutorService executor = invoke(Executors.class.getMethod("newVirtualThreadPerTaskExecutor"), null);
            AlixCommonMain.logInfo("Using VirtualThreads for blocking tasks execution.");
            return executor;
        } catch (NoSuchMethodException e) {
            AlixCommonMain.logInfo("Using a fixed pool for blocking tasks execution.");
            return Executors.newFixedThreadPool(4);
        }
    }

    public static AbstractBanList createBanListImpl(boolean ip) {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                return ip ? BukkitBanList.IP : BukkitBanList.NAME;
            default:
                throw new AssertionError();
        }
    }

    public static ConfigProvider createConfigProviderImpl() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                return new BukkitConfigImpl();
            case VELOCITY:
                return new VelocityConfigImpl();
            default:
                throw new AssertionError();
        }
    }

    public static AbstractServer createServerAccessorImpl() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                return new BukkitServer();
            /*case VELOCITY:
                return VelocityAlixMain.instance;*/
            default:
                throw new AssertionError();
        }
    }

    /*public static LoaderBootstrap getBootstrap() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                return BukkitAlixMain.instance.getPlugin();
            case VELOCITY:
                return VelocityAlixMain.instance.getPlugin();
            default:
                throw new AssertionError();
        }
    }*/

    public static AlixMain getMainClassInstance() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                return BukkitAlixMain.instance;
            case VELOCITY:
                return VelocityAlixMain.instance;
            default:
                throw new AssertionError();
        }
    }

    public static AlixLoggerProvider getLoggerProvider() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                return BukkitAlixMain.instance;
            case VELOCITY:
                return VelocityAlixMain.instance;
            default:
                throw new AssertionError();
        }
    }

    private AlixCommonHandler() {
    }
}