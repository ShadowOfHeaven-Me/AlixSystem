package alix.common.utils;

import alix.common.AlixCommonMain;
import alix.common.AlixMain;
import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.connection.ConnectionThreadManager;
import alix.common.connection.filters.AntiVPN;
import alix.common.connection.filters.ConnectionManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.environment.ServerEnvironment;
import alix.common.logger.AlixLoggerProvider;
import alix.common.login.prelogin.PreLoginVerdict;
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

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static alix.common.reflection.CommonReflection.invoke;
import static alix.common.utils.AlixCommonHandler.DoNotThrow.*;
import static alix.common.utils.config.ConfigProvider.config;

public final class AlixCommonHandler {

    static final class DoNotThrow {
        static final boolean antibotService = config.getBoolean("antibot-service", true);
        static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]*");
        static final boolean validateName = config.getBoolean("validate-name");
    }

    public static PreLoginVerdict getPreLoginVerdict(InetAddress address, String nameSent, String nameRefactored, PersistentUserData data, boolean deemedPremium) {
        if (data == null && validateName && (nameSent.length() > 16 || nameSent.isEmpty() || !NAME_PATTERN.matcher(nameSent).matches())) {
            //FireWallManager.add(user.getAddress().getAddress(), "E1");
            //event.setLastUsedWrapper(INVALID_LOGIN_WRAPPER);//we know it'll throw an exception during a normal read, so if other plugins try to read it they should check for nulls (although they are more than likely to be using a separate PE instance, but not much I can do about that)
            //event.setCancelled(true);
            return PreLoginVerdict.DISALLOWED_INVALID_NAME;
        }

        //String name = Dependencies.getCorrectUsername(channel, name); //Dependencies.FLOODGATE_PREFIX != null && Dependencies.isBedrock(channel) ? Dependencies.FLOODGATE_PREFIX + name : name;
        //String name = name;

        //AlixScheduler.async(() -> (?)

        if (antibotService)
            ConnectionThreadManager.onJoinAttempt(nameRefactored, address);

        if (data == null) {
            if (!deemedPremium && AntiBotStatistics.INSTANCE.isHighTraffic() && ConnectionManager.disallowJoin(nameRefactored)) {//Close the connection before the auth thread is started
                //event.setCancelled(true);
                return PreLoginVerdict.DISALLOWED_PREVENT_FIRST_JOIN;
            }

            if (GeoIPTracker.disallowJoin(address, nameRefactored)) {
                //event.setCancelled(true);
                return PreLoginVerdict.DISALLOWED_MAX_ACCOUNTS_REACHED;
            }

            if (AntiVPN.disallowJoin(address, nameRefactored)) {
                //event.setCancelled(true);
                return PreLoginVerdict.DISALLOWED_VPN_DETECTED;
            }
        }
        return PreLoginVerdict.ALLOWED;
    }

    public static boolean preferDirectBufs() {
        switch (ServerEnvironment.getEnvironment()) {
            case VELOCITY: {
                return VelocityAlixMain.nativePreferDirectBufs();
            }
            default:
                return true;
        }
    }

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

    @SuppressWarnings("JavaReflectionMemberAccess")
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