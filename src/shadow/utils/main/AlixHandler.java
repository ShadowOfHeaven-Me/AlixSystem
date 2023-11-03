package shadow.utils.main;

import alix.common.antibot.connection.ConnectionFilter;
import alix.common.antibot.connection.types.AntiVPN;
import alix.common.antibot.connection.types.ConnectionManager;
import alix.common.antibot.connection.types.GeoIPTracker;
import alix.common.antibot.connection.types.ServerPingManager;
import alix.common.messages.Messages;
import alix.common.messages.types.AlixMessage;
import alix.common.scheduler.impl.AlixScheduler;
import alix.common.utils.AlixConsoleFilter;
import alix.common.utils.formatter.AlixFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import shadow.Main;
import shadow.systems.executors.OfflineExecutors;
import shadow.systems.executors.OnlineExecutors;
import shadow.systems.executors.ServerPingListener;
import shadow.systems.executors.gui.GUIExecutors;
import shadow.systems.login.Verifications;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.utils.command.managers.ChatManager;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.objects.filter.packet.check.PacketAffirmator;
import shadow.utils.objects.filter.packet.check.impl.MultiVersionPacketAffirmator;
import shadow.utils.objects.filter.packet.check.impl.NewerVersionedPacketAffirmator;
import shadow.utils.objects.filter.packet.injector.ChannelInjector;
import shadow.utils.objects.filter.packet.injector.ChannelInjectorNMS;
import shadow.utils.objects.filter.packet.injector.ChannelInjectorPacketEvents;
import shadow.utils.objects.filter.packet.types.PacketBlocker;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.users.UserManager;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static shadow.utils.main.AlixUtils.*;

public class AlixHandler {

    private static final AlixMessage
            chatSetOff = Messages.getAsObject("chat-set-off"),
            chatSetOn = Messages.getAsObject("chat-set-on"),
            playerWasDeopped = Messages.getAsObject("player-was-deopped"),
            playerWasOpped = Messages.getAsObject("player-was-opped");


    private static final long teleportDelay = Main.config.getLong("user-teleportation-delay");
    private static final boolean isTeleportDelayed = teleportDelay > 0;
    private static final String delayedTeleportMessage = Messages.getWithPrefix("user-delayed-teleportation", AlixUtils.formatMillis(teleportDelay));

    public static void delayedConfigTeleportExecute(Runnable r, Player p) {
        if (isTeleportDelayed) AlixScheduler.runLaterSync(r, teleportDelay, TimeUnit.MILLISECONDS);
        else r.run();
        p.sendRawMessage(delayedTeleportMessage);
    }

    public static void delayedConfigTeleport(Player player, Location loc) {
        if (isTeleportDelayed) {
            AlixScheduler.runLaterSync(() -> player.teleport(loc), teleportDelay, TimeUnit.MILLISECONDS);
            player.sendRawMessage(delayedTeleportMessage);
        } else player.teleport(loc);
    }

    public static ConnectionFilter[] getConnectionFilters() {//set up in the most efficient way
        List<ConnectionFilter> filters = new ArrayList<>();
        if (ServerPingManager.isRegistered()) filters.add(ServerPingManager.instance);
        if (Main.config.getBoolean("prevent-first-time-join")) filters.add(new ConnectionManager());
        if (maximumTotalAccounts > 0) filters.add(GeoIPTracker.instance);
        if (Main.config.getBoolean("anti-vpn")) filters.add(new AntiVPN());
        return filters.toArray(new ConnectionFilter[0]);
    }

    public static ConnectionFilter[] getPremiumConnectionFilters() {//set up in the most efficient way
        List<ConnectionFilter> filters = new ArrayList<>();
        if (maximumTotalAccounts > 0) filters.add(GeoIPTracker.instance);
        if (Main.config.getBoolean("anti-vpn")) filters.add(new AntiVPN());
        return filters.toArray(new ConnectionFilter[0]);
    }

    public static void initExecutors(PluginManager pm) {
        if (isOfflineExecutorRegistered) {
            pm.registerEvents(new OfflineExecutors(), Main.plugin);
            pm.registerEvents(new GUIExecutors(), Main.plugin);
            /*if (anvilPasswordGui) {
                pm.registerEvents(new AnvilGuiExecutors(), Main.plugin);
                Main.logError("drftgyhjuik");
            }*/
            if (ServerPingManager.isRegistered()) pm.registerEvents(new ServerPingListener(), Main.plugin);

            Listener listener = PremiumAutoIn.getAutoLoginListener();

            if (listener != null) pm.registerEvents(listener, Main.plugin);
        } else {
            pm.registerEvents(new OnlineExecutors(), Main.plugin);
        }
    }

    public static void addConsoleFilter() {
        ((Logger) LogManager.getRootLogger()).addFilter(new AlixConsoleFilter());
        Main.debug(isPluginLanguageEnglish ? "Successfully implemented AlixConsoleFilter to console!" :
                "Poprawnie zaimplementowano AlixConsoleFilter dla konsoli! ");
    }

    public static void initializeServerPingManager() {
        ServerPingManager.init();
        //JavaScheduler.repeatAsync(ServerPingManager::clear, 10, TimeUnit.MINUTES);
    }

    public static UnverifiedUser handleOfflinePlayerJoin(Player p, String joinMessage) {
        String name = p.getName();

        if (PremiumAutoIn.remove(name)) {//The given user is premium
            //Main.logInfo("" + PremiumAutoIn.isPremium(p.getUniqueId()));
            UserManager.addOnlineUser(p);
            return null;
        }

        //if (PremiumAutoIn.isPremium(p.getUniqueId())) return;

        //if (UserManager.getNullableUserOnline(p) != null) return; //No fast login, no need to check this (way before time he FastLogin AuthPlugin used to add

        PersistentUserData data = UserFileManager.get(name);

        String address = p.getAddress().getAddress().getHostAddress();

        if (data == null) {//not registered - no data
            GeoIPTracker.addTempIP(address);
            return Verifications.add(p, data, address, joinMessage);
        }

        if (!data.getPassword().isSet()) {//not registered - password was reset
            return Verifications.add(p, data, address, joinMessage);
        }

        if (playerIPAutoLogin && data.getSavedIP().equals(address)) {//ip auto login
            UserManager.addOfflineUser(p, data, address);
            p.sendMessage(Messages.autoLoginMessage);
            return null;
        }

        return Verifications.add(p, data, address, joinMessage); //not logged in
    }

    public static void handleOfflinePlayerQuit(Player p, PlayerQuitEvent event) {
        UnverifiedUser u = Verifications.remove(p);

        if (u != null) {//unregistered/not logged in user removal
            u.uninject();//do not remove the netty channel blocker, as all netty channels are automatically disconnected upon player server quit
            u.getPacketBlocker().cancelLoginKickTask();
            if (!u.hasAccount()) {//quit after join with no account was made (the account could've been created later on, but that is no concern to us)
                GeoIPTracker.removeTempIP(u.getIPAddress());//removing the temporary ip
                if (requireCaptchaVerification)
                    event.setQuitMessage(null);//removing the quit message whenever the join message was also removed
            }
        } else UserManager.remove(p);//logged in user removal
    }

    public static PacketAffirmator createPacketAffirmatorImpl() {
        if (PacketBlocker.serverboundVersion) {
            Main.logInfo("Using the 1.17+ packet affirmator for packet processing.");
            return new NewerVersionedPacketAffirmator();
        }
        Main.logInfo("Using the multi-version packet affirmator for packet processing.");
        return new MultiVersionPacketAffirmator();
    }

    public static ChannelInjector createChannelInjectorImpl() {
        if (Main.pm.isPluginEnabled("packetevents")) {
            Main.logInfo("Using PacketEvents for channel injecting (provided).");
            return new ChannelInjectorPacketEvents();
        }
        Main.logInfo("Using NMS for channel injecting (built-in).");
        return new ChannelInjectorNMS();
    }

/*    public static PingCheckFactory createPingCheckFactoryImpl() {
        if (ReflectionUtils.protocolVersion) {
            Main.logInfo("Using the optimized (1.17+) PingCheckFactory for ping checking tasks");
            return new OptimizedPingCheckFactoryImpl();
        }
        Main.logInfo("Using the unoptimized multi-version PingCheckFactory for ping checking tasks - 1.17+ is suggested for better performance.");
        return new MultiVersionPingCheckFactoryImpl();
    }*/

    public static boolean borrowCommandExecutorIfRegistered(String commandLabel) {
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
    }

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

    public static void handleOperatorSetEN(CommandSender sender, String arg1) {
        boolean success = setOperator(arg1);
        if (success) {
            //sendMessage(sender, "Successfully opped " + arg1 + "!");
            broadcastColorizedToPermitted(playerWasOpped.format(arg1, sender.getName()));
            return;
        }
        sendMessage(sender, "&cCould not op player " + arg1 + "! Please check if there are any errors in the console!");
    }

    public static void handleOperatorUnsetEN(CommandSender sender, String arg1) {
        boolean success = unsetOperator(arg1);
        if (success) {
            //sendMessage(sender, "Successfully deopped " + arg1 + "!");
            broadcastColorizedToPermitted(playerWasDeopped.format(arg1, sender.getName()));
            return;
        }
        sendMessage(sender, "&cCould not deop player " + arg1 + "! Please check if there are any errors in the console!");
    }

    public static void handleOperatorSetPL(CommandSender sender, String arg1) {
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
    }

    public static void kickAll(String reason) {
        String kickMessage = AlixFormatter.appendPrefix(reason);
        for (Player p : Bukkit.getOnlinePlayers()) p.kickPlayer(kickMessage);
    }
}