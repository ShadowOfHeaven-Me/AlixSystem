package shadow.utils.main;

import shadow.systems.netty.AlixInterceptor;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.other.throwable.AlixError;
import io.netty.channel.ChannelFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.executors.OfflineExecutors;
import shadow.systems.executors.OnlineExecutors;
import shadow.systems.executors.ServerPingListener;
import shadow.systems.executors.gui.GUIExecutors;
import shadow.systems.login.Verifications;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.systems.login.filters.*;
import shadow.systems.login.firewall.DelayedChannelFireWall;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.command.managers.ChatManager;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.main.file.managers.OriginalLocationsManager;
import shadow.utils.objects.AlixConsoleFilterHolder;
import shadow.utils.objects.packet.check.PacketAffirmator;
import shadow.utils.objects.packet.check.impl.MultiVersionPacketAffirmator;
import shadow.utils.objects.packet.check.impl.NewerVersionedPacketAffirmator;
import shadow.utils.objects.packet.injector.ChannelInjector;
import shadow.utils.objects.packet.injector.ChannelInjectorNMS;
import shadow.utils.objects.packet.injector.ChannelInjectorPacketEvents;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.users.User;
import shadow.utils.users.UserManager;
import shadow.utils.users.offline.UnverifiedUser;
import shadow.utils.world.AlixWorld;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static shadow.utils.main.AlixUtils.*;

public final class AlixHandler {

    private static final AlixMessage
            chatSetOff = Messages.getAsObject("chat-set-off"),
            chatSetOn = Messages.getAsObject("chat-set-on"),
            playerWasDeopped = Messages.getAsObject("player-was-deopped"),
            playerWasOpped = Messages.getAsObject("player-was-opped"),
            quitCaptchaUnverified = Messages.getAsObject("log-player-quit-captcha-unverified"),
            quitUnverified = Messages.getAsObject("log-player-quit-unverified"),
            quitVerified = Messages.getAsObject("log-player-quit-verified");


    private static final long teleportDelay = Main.config.getLong("user-teleportation-delay");
    private static final boolean isTeleportDelayed = teleportDelay > 0;
    private static final String delayedTeleportMessage = Messages.getWithPrefix("user-delayed-teleportation", AlixUtils.formatMillis(teleportDelay));

    private static void initializeAlixInjector() {
        try {
            Class<?> mcServerClass = ReflectionUtils.nms2("server.MinecraftServer");
            Object mcServer = mcServerClass.getMethod("getServer").invoke(null);

            Class<?> serverConnectionClass = ReflectionUtils.nms2("server.network.ServerConnection");
            Object serverConnection = null;

            //ServerConnection c;

            for (Field f : mcServerClass.getDeclaredFields()) {
                if (f.getType() == serverConnectionClass) {
                    f.setAccessible(true);
                    serverConnection = f.get(mcServer);
                    break;
                }
            }
            if (serverConnection == null) throw new AlixError();

            for (Field f : serverConnectionClass.getDeclaredFields()) {
                if (f.getType() == List.class && ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0] == ChannelFuture.class) {//we know it's a parameterized type since it's a List
                    f.setAccessible(true);
                    List<ChannelFuture> futures = (List<ChannelFuture>) f.get(serverConnection);//it's a List only because of reused code from minecraft singleplayer (info from Emily)
                    ChannelFuture future = futures.get(0);//it should contain only one handler

                    boolean fastRaw = Main.config.getBoolean("fast-raw-firewall");
                    AlixInterceptor.injectIntoServerPipeline(future.channel().pipeline(), fastRaw ? null : DelayedChannelFireWall.INSTANCE);//make sure we handle all of the connection requests before anyone else
                    break;
                }
            }
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }

    public static void delayedConfigTeleportExecute(Runnable r, Player p) {
        if (isTeleportDelayed) AlixScheduler.runLaterSync(r, teleportDelay, TimeUnit.MILLISECONDS);
        else r.run();
        p.sendRawMessage(delayedTeleportMessage);
    }

    public static void delayedConfigTeleport(Player player, Location loc) {
        if (isTeleportDelayed) {
            AlixScheduler.runLaterSync(() -> MethodProvider.teleportAsync(player, loc), teleportDelay, TimeUnit.MILLISECONDS);
            player.sendRawMessage(delayedTeleportMessage);
        } else MethodProvider.teleportAsync(player, loc);
    }

/*    public static ConnectionFilter[] getConnectionFilters() {//set up in the most efficient way
        List<ConnectionFilter> filters = new ArrayList<>();
        if (ServerPingManager.isRegistered()) filters.add(ServerPingManager.instance);
        if (Main.config.getBoolean("prevent-first-time-join")) filters.add(new ConnectionManager());
        if (maximumTotalAccounts > 0) filters.add(GeoIPTracker.instance);
        if (Main.config.getBoolean("anti-vpn")) filters.add(AntiVPN.INSTANCE);
        return filters.toArray(new ConnectionFilter[0]);
    }*/

    public static ConnectionFilter[] getConnectionFilters() {//set up in the most efficient way
        List<ConnectionFilter> filters = new ArrayList<>();
        if (ServerPingManager.isRegistered()) filters.add(ServerPingManager.INSTANCE);
        if (Main.config.getBoolean("prevent-first-time-join")) filters.add(new ConnectionManager());
        if (maximumTotalAccounts > 0) filters.add(GeoIPTracker.INSTANCE);
        if (Main.config.getBoolean("anti-vpn")) filters.add(AntiVPN.INSTANCE);
        return filters.toArray(new ConnectionFilter[0]);
    }

    public static ConnectionFilter[] getPremiumConnectionFilters() {//set up in the most efficient way
        List<ConnectionFilter> filters = new ArrayList<>();
        if (maximumTotalAccounts > 0) filters.add(GeoIPTracker.INSTANCE);
        if (Main.config.getBoolean("anti-vpn")) filters.add(AntiVPN.INSTANCE);
        return filters.toArray(new ConnectionFilter[0]);
    }

    public static void initExecutors(PluginManager pm) {
        if (isOfflineExecutorRegistered) {
            pm.registerEvents(new OfflineExecutors(), Main.plugin);

            initializeAlixInjector();
            //PaperAccess.initializeFireWall();
                /*if (ServerEnvironment.getEnvironment() == ServerEnvironment.PAPER)
                    PaperAccess.initializeFireWall();
                else if (Dependencies.isPacketEventsPresent) PacketEventsAccess.initializeFireWall();
                else {
                    Main.logError("[---------------------------------------------------------------------------------------------------]");
                    Main.logError("                        +=======================================+");
                    Main.logError("                        |                " + AlixLoggerProvider.wrap("WARNING", ConsoleColor.BRIGHT_RED) + "                |");
                    Main.logError("                        +=======================================+");
                    Main.logError("");
                    Main.logError("                      Unable to initialize the FireWall Protection!");
                    Main.logError("Either switch to " + AlixLoggerProvider.wrap("Paper", ConsoleColor.BRIGHT_CYAN) + " (server software) or install " + AlixLoggerProvider.wrap("PacketEvents", ConsoleColor.BRIGHT_CYAN) + " if you wish to use this function!");
                    Main.logError("                   If you're absolutely sure you don't want to use this function,");
                    Main.logError("                         set 'antibot-service' in config.yml to false.");
                    Main.logError("");
                    Main.logError("[---------------------------------------------------------------------------------------------------]");
                }*/

            /*if (Dependencies.isPacketEventsPresent) new PacketEventsListener();
            else */
            pm.registerEvents(new GUIExecutors(), Main.plugin);
            /*if (anvilPasswordGui) {
                pm.registerEvents(new AnvilGuiExecutors(), Main.plugin);
                Main.logError("drftgyhjuik");
            }*/
            pm.registerEvents(new ServerPingListener(), Main.plugin);
            /*            if (requireCaptchaVerification) {
             *//*if(ServerEnvironment.getEnvironment() == ServerEnvironment.PAPER) {
                    Main.logInfo("Enabling Async Tab Completion support.");
                    pm.registerEvents(new PaperTabCompletionExecutors(), Main.plugin);
                } else *//*
                //pm.registerEvents(new BukkitTabCompletionExecutors(), Main.plugin);
            }*/

            Listener listener = PremiumAutoIn.getAutoLoginListener();

            if (listener != null) pm.registerEvents(listener, Main.plugin);
        } else pm.registerEvents(new OnlineExecutors(), Main.plugin);
    }

    public static void updateConsoleFilter() {
        Logger logger = ((Logger) LogManager.getRootLogger());
        for (Iterator<Filter> it = logger.getFilters(); it.hasNext(); ) {
            Filter filter = it.next();
            if (filter instanceof AlixConsoleFilterHolder) {
                ((AlixConsoleFilterHolder) filter).updateInstance();//updating the instance, since it's bytecode could've changed after a reload
                return;
            }
        }
        if (hideFailedJoinAttempts || alixJoinLog) {
            logger.addFilter(new AlixConsoleFilterHolder());
            Main.debug(isPluginLanguageEnglish ? "Successfully implemented AlixConsoleFilter to console!" :
                    "Poprawnie zaimplementowano AlixConsoleFilter dla konsoli! ");
        }
    }

    public static void initializeServerPingManager() {
        ServerPingManager.init();
        //JavaScheduler.repeatAsync(ServerPingManager::clear, 10, TimeUnit.MINUTES);
    }

    public static UnverifiedUser handleOfflinePlayerJoin(Player p, String joinMessage, LoginInfo login) {
        if (login.getVerdict().isAutoLogin() && p.getWorld().getUID().equals(AlixWorld.CAPTCHA_WORLD.getUID())) //tp back if there was an issue with the teleportation beforehand
            OriginalLocationsManager.teleportBack(p, true);
        switch (login.getVerdict()) {//a fast injection based on the login verdict
            case DISALLOWED_NO_DATA:
                GeoIPTracker.addTempIP(login.getIP());
                return Verifications.add(p, login, joinMessage);
            case DISALLOWED_PASSWORD_RESET://needs to register
            case DISALLOWED_LOGIN_REQUIRED://needs to log in
                return Verifications.add(p, login, joinMessage);
            case REGISTER_PREMIUM:
                autoRegisterCommandList.invoke(p);
                UserManager.addOnlineUser(p, login);
                return null;
            case LOGIN_PREMIUM:
                autoLoginCommandList.invoke(p);
                UserManager.addOnlineUser(p, login);
                return null;
            case IP_AUTO_LOGIN:
                autoLoginCommandList.invoke(p);
                UserManager.addOfflineUser(p, login.getData(), login.getIP(), login.getPacketInterceptor());
                p.sendMessage(Messages.autoLoginMessage);
                return null;
            default:
                throw new AssertionError("Invalid: " + login.getVerdict());
        }
    }

    public static void handleOfflinePlayerQuit(Player p, PlayerQuitEvent event) {
        UnverifiedUser u = Verifications.remove(p);

        if (u != null) {//unregistered/not logged in user removal
            u.uninject();//uninject the user
            //u.getPacketBlocker().cancelLoginKickTask();
            if (u.isCaptchaInitialized()) {//quit after captcha was initialized, and the user hasn't registered, occurred
                GeoIPTracker.removeTempIP(u.getIPAddress());
                event.setQuitMessage(null);//removing the quit message whenever the join message was also removed and never sent
                if (alixJoinLog) Main.logInfo(quitCaptchaUnverified.format(p.getName(), u.getIPAddress()));
            } else if (alixJoinLog) Main.logInfo(quitUnverified.format(p.getName(), u.getIPAddress()));
        } else {
            User user = UserManager.remove(p);//logged in user removal
            if (user == null)
                Main.logWarning("Could not get any User instance for the player " + p.getName() + " on quit!");
            else Main.logInfo(quitVerified.format(user.getName(), user.getIPAddress()));
            //return;
        }
        /*if (!alixJoinLog) return;

        String name = p.getName();
        String ip = u != null ? u.getIPAddress() : user.getIPAddress();
        AlixMessage message =*/
    }

    public static PacketAffirmator createPacketAffirmatorImpl() {
        if (PacketBlocker.serverboundNameVersion) {
            Main.logInfo("Using the 1.17+ packet affirmator for packet processing.");
            return new NewerVersionedPacketAffirmator();
        }
        Main.logInfo("Using the multi-version packet affirmator for packet processing.");
        return new MultiVersionPacketAffirmator();
    }

    public static ChannelInjector createChannelInjectorImpl() {
        if (Dependencies.isPacketEventsPresent) {
            Main.logInfo("Using PacketEvents for channel injecting (provided).");
            return new ChannelInjectorPacketEvents();
        }
        /*if (ServerEnvironment.getEnvironment() == ServerEnvironment.PAPER) {
            Main.logInfo("Using Paper for channel injecting (built-in).");
            return new ChannelInjectorPaper();
        }*/
        ChannelInjector injector = new ChannelInjectorNMS();
        Main.logInfo("Using " + injector.getProvider() + " for channel injecting (built-in).");
        return injector;
    }

/*    public static PingCheckFactory createPingCheckFactoryImpl() {
        if (ReflectionUtils.protocolVersion) {
            Main.logInfo("Using the optimized (1.17+) PingCheckFactory for ping checking tasks");
            return new OptimizedPingCheckFactoryImpl();
        }
        Main.logInfo("Using the unoptimized multi-version PingCheckFactory for ping checking tasks - 1.17+ is suggested for better performance.");
        return new MultiVersionPingCheckFactoryImpl();
    }*/

/*    public static boolean borrowCommandExecutorIfRegistered(String commandLabel) {
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            String name = p.getName();

            if (name.equals(Main.plugin.getName())) continue;

            String fallbackPrefix = name.toLowerCase();

            PluginCommand cmd = Bukkit.getPluginCommand(fallbackPrefix + ":" + commandLabel);

            if (cmd == null) continue;

            PluginCommand alixCommand = Bukkit.getPluginCommand(Main.plugin.getName().toLowerCase() + ":" + commandLabel);

            alixCommand.setExecutor(cmd.getExecutor());
            return true;
        }
        return false;
    }*/

/*    public static void findAndSetFallbackCommandExecutor(CommandMap map, Command cmd, String label) {
        Command primary = map.getCommand("minecraft:" + label);
        String fallbackPrefix = "minecraft";
        //CommandExecutor executor = primary.
        if (primary == null) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.equals(Main.plugin)) continue;
                String f2 = plugin.getName().toLowerCase();
                Command c2 = map.getCommand(f2 + ":" + label);
                if (c2 != null) {
                    primary = c2;
                    fallbackPrefix = f2;
                }
            }
        }
        if (primary == null) return;
        map.register(label, fallbackPrefix, primary);
    }*/

    public static void handleChatTurnOn(CommandSender sender) {
        if (ChatManager.isChatTurnedOn()) {
            sendMessage(sender, Messages.chatAlreadyOn);
            return;
        }
        ChatManager.setChatTurnedOn(true, sender);
        broadcastColorized(chatSetOn.format(sender.getName()));
    }

    public static void handleChatTurnOff(CommandSender sender) {
        if (!ChatManager.isChatTurnedOn()) {
            sendMessage(sender, Messages.chatAlreadyOff);
            return;
        }
        ChatManager.setChatTurnedOn(false, sender);
        broadcastColorized(chatSetOff.format(sender.getName()));
    }

    private static boolean setOperator(String who) {
        //OperatorCommandTabCompleter.add(who);
        return dispatchServerCommand("minecraft:op " + who);
    }

    private static boolean unsetOperator(String who) {
        //OperatorCommandTabCompleter.remove(who);
        return dispatchServerCommand("minecraft:deop " + who);
    }

    public static void handleOperatorSet(CommandSender sender, String arg1) {
        boolean success = setOperator(arg1);
        if (success) {
            //sendMessage(sender, "Successfully opped " + arg1 + "!");
            broadcastColorizedToPermitted(playerWasOpped.format(arg1, sender.getName()));
            return;
        }
        sendMessage(sender, "&cCould not op player " + arg1 + "! Please check if there are any errors in the console!");
    }

    public static void handleOperatorUnset(CommandSender sender, String arg1) {
        boolean success = unsetOperator(arg1);
        if (success) {
            //sendMessage(sender, "Successfully deopped " + arg1 + "!");
            broadcastColorizedToPermitted(playerWasDeopped.format(arg1, sender.getName()));
            return;
        }
        sendMessage(sender, "&cCould not deop player " + arg1 + "! Please check if there are any errors in the console!");
    }

/*    public static void handleOperatorSetPL(CommandSender sender, String arg1) {
        boolean success = setOperator(arg1);
        if (success) {
            //sendMessage(sender, "Ustawianie operatora dla " + arg1 + " powiodło się!");
            broadcastColorizedToPermitted("&7Gracz " + arg1 + " został operatorem dzięki " + sender.getName());
            return;
        }
        sendMessage(sender, "&cWystąpił błąd przy ustawianiu operatora dla " + arg1 + "! Proszę sprawdzić czy w konsoli ukazały się jakiekolwiek errory!");
    }

    public static void handleOperatorUnsetPL(CommandSender sender, String arg1) {
        boolean success = unsetOperator(arg1);
        if (success) {
            //sendMessage(sender, "Odbieranie operatora dla " + arg1 + " powiodło się!");
            broadcastColorizedToPermitted("&7Operator dla gracza " + arg1 + " został odebrany dzięki " + sender.getName());
            return;
        }
        sendMessage(sender, "&cWystąpił błąd przy odbieraniu operatora dla " + arg1 + "! Proszę sprawdzić czy w konsoli ukazały się jakiekolwiek errory!");
    }*/

    public static void kickAll(String reason) {
        String kickMessage = AlixFormatter.appendPrefix(reason);
        for (Player p : Bukkit.getOnlinePlayers()) p.kickPlayer(kickMessage);
    }

    private AlixHandler() {
    }
}