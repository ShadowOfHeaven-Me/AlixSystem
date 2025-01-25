package shadow.utils.main;

import alix.api.event.AlixEvent;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.connection.filters.ServerPingManager;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixError;
import alix.spigot.api.events.AuthReason;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.epoll.EpollServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import shadow.Main;
import shadow.systems.executors.OfflineExecutors;
import shadow.systems.executors.PaperSpawnExecutors;
import shadow.systems.executors.ServerPingListener;
import shadow.systems.executors.SpigotSpawnExecutors;
import shadow.systems.executors.gui.GUIExecutors;
import shadow.systems.login.captcha.types.CaptchaVisualType;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.command.managers.ChatManager;
import shadow.utils.main.api.AlixEventInvoker;
import shadow.utils.misc.ReflectionUtils;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutEntityPacketConstructor;
import shadow.utils.misc.packet.constructors.OutGameStatePacketConstructor;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.objects.AlixConsoleFilterHolder;
import shadow.utils.users.UserManager;
import shadow.utils.users.Verifications;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.users.types.VerifiedUser;
import shadow.utils.world.AlixWorld;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
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
            playerQuit = Messages.getAsObject("log-player-quit");

    private static final long teleportDelay = Main.config.getLong("user-teleportation-delay");
    private static final boolean isTeleportDelayed = teleportDelay > 0;
    private static final String delayedTeleportMessage = Messages.get("user-delayed-teleportation", AlixUtils.formatMillis(teleportDelay));
    //public static final ChannelFuture SERVER_CHANNEL_FUTURE = getServerChannelFuture();
    public static final Channel SERVER_CHANNEL = getServerChannelFuture().channel();
    public static final boolean isEpollTransport = SERVER_CHANNEL.getClass() == EpollServerSocketChannel.class;

    public static void resetBlindness(UnverifiedUser user) {
        if (!user.blindnessSent) return;
        user.writeAndFlushDynamicSilently(new WrapperPlayServerRemoveEntityEffect(user.getPlayer().getEntityId(), PotionTypes.BLINDNESS));
    }

    /*public static void resetLoginEffectPackets(UnverifiedUser user) {
        Player player = user.getPlayer();

        //Object removeBlindnessPacket = outRemoveEntityEffectPacketConstructor.newInstance(player.getEntityId(), BLINDNESS_MOB_EFFECT_LIST);
//show players in tab
        ByteBuf[] addPlayersBuffers = OutPlayerInfoPacketConstructor.construct_ADD_OF_ALL_VISIBLE(user.getPlayer(), user.silentContext());
        //reset blindness effect
        ByteBuf removeBlindnessBuffer = user.blindnessSent ? NettyUtils.dynamic(new WrapperPlayServerRemoveEntityEffect(player.getEntityId(), PotionTypes.BLINDNESS), user.silentContext()) : null;
        //synchronize the tab information for the unverified user
        user.getChannel().eventLoop().execute(() -> {
            try {
                for (ByteBuf buf : addPlayersBuffers) user.writeSilently(buf);
                if (removeBlindnessBuffer != null) user.writeSilently(removeBlindnessBuffer);
                user.flush();
            } catch (Throwable e) {
                Main.logError("No jak chuj coś tu nie działa");
                e.printStackTrace();
            }
        });

        //show the verified users the unverified user in tab
        ByteBuf[] addUnvPlayerBuffersConst = OutPlayerInfoPacketConstructor.constructConst_ADD_OF_ONE_VISIBLE(user.reetrooperUser(), user.getPlayer());

        for (AlixUser u : UserManager.users())
            if (u instanceof VerifiedUser && OutPlayerInfoPacketConstructor.isVisible(((VerifiedUser) u).getPlayer(), player)) {
                for (ByteBuf buf : addUnvPlayerBuffersConst) u.writeConstSilently(buf);
                u.flush();
            }

        for (ByteBuf buf : addUnvPlayerBuffersConst) buf.unwrap().release();
    }*/

    private static final boolean sendBlindness = CaptchaVisualType.shouldSendBlindness();
    //private static final ByteBuf TIME_NIGHT = NettyUtils.constBuffer(new WrapperPlayServerTimeUpdate(0, 18000));

/*
    static {
        Main.logError("BLINDNESS " + sendBlindness);
    }
*/

    private static float getScope(double level) {
        if (level < 1 || level > 10) {
            throw new IllegalArgumentException("1..10, found " + level);
        }
        return (float) (1.0 / (20 / level - 10)); // checking for division by zero is not needed here, Java gives Infinity when dividing by zero. ABILITIES packet correctly understands the meaning of Infinity
    }
    //private static final ByteBuf VER_POS = NettyUtils.constBuffer(new WrapperPlayServerPo);
    //SpigotConversionUtil.

    /*potion effect ids:
    slowness = 2
    jump boost = 8
    */
    public static final int KEEP_ALIVE_ID = 2137;
    private static final ByteBuf ANTIBOT_KEEP_ALIVE = NettyUtils.constBuffer(new WrapperPlayServerKeepAlive(KEEP_ALIVE_ID));

    private static final int spectateEntityId = 420_096;
    private static final ByteBuf SPAWN_ENTITY = OutEntityPacketConstructor.constSpawn(spectateEntityId, EntityTypes.ARMOR_STAND, AlixWorld.TELEPORT_LOCATION);
    private static final ByteBuf SPAWN_ENTITY_INVIS = OutEntityPacketConstructor.constData(spectateEntityId, new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20));
    private static final ByteBuf SPECTATE_ENTITY = NettyUtils.constBuffer(new WrapperPlayServerCamera(spectateEntityId));

    //private static final ByteBuf SPECTATE_ABILITIES_PACKET = NettyUtils.constBuffer(new WrapperPlayServerPlayerAbilities(true, true, false, false, 0.0f, getScope(5)));//default: fovModifier = 0.1f, flySpeed = 0.05f
    private static final ByteBuf PLAYER_ABILITIES_PACKET = NettyUtils.constBuffer(new WrapperPlayServerPlayerAbilities(true, true, false, false, 0.0f, getScope(1)));//default: flySpeed = 0.05f, fovModifier = 0.1f

    public static void sendLoginEffectsPackets(UnverifiedUser user) {
        if (!user.hasCompletedCaptcha()) {
            /*if (true) {//for testing
                user.writeConstSilently(OutGameStatePacketConstructor.SPECTATOR_GAMEMODE_PACKET);
                user.writeAndFlushDynamicSilently(new WrapperPlayServerPlayerAbilities(true, true, true, false, 0.05f, 0.1f));
                return;
            }*/

            //user.writeDynamicSilently(new WrapperPlayServerEntityAnimation(user.getPlayer().getEntityId(), WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM));

            if (CaptchaVisualType.hasPositionLock()) {
                user.writeConstSilently(OutGameStatePacketConstructor.SPECTATOR_GAMEMODE_PACKET);
                user.writeConstSilently(SPAWN_ENTITY);
                user.writeConstSilently(SPAWN_ENTITY_INVIS);
                user.writeConstSilently(SPECTATE_ENTITY);
            } else user.writeConstSilently(OutGameStatePacketConstructor.ADVENTURE_GAMEMODE_PACKET);
            //user.writeConstSilently(AlixHandler.ANTIBOT_KEEP_ALIVE);
            user.writeAndFlushConstSilently(PLAYER_ABILITIES_PACKET);

            //user.armSwingSent = System.currentTimeMillis();
            //user.keepAliveSent = System.currentTimeMillis();

            user.getChannel().eventLoop().schedule(() -> {
                if (user.getChannel().isOpen()) user.writeAndFlushConstSilently(ANTIBOT_KEEP_ALIVE);
            }, 200, TimeUnit.MILLISECONDS);//being really generous with the amount of time here

            //Main.debug("SENT ARM SWING AND KEEP ALIVE");
            return;
        }
        user.writeConstSilently(OutGameStatePacketConstructor.ADVENTURE_GAMEMODE_PACKET);
        user.writeAndFlushConstSilently(PLAYER_ABILITIES_PACKET);
        //user.writeAndFlushConstSilently(TIME_NIGHT);

        if (sendBlindness)
            sendBlindnessPacket(user);
    }

    private static void sendBlindnessPacket(UnverifiedUser user) {
        user.writeAndFlushDynamicSilently(new WrapperPlayServerEntityEffect(user.getPlayer().getEntityId(), PotionTypes.BLINDNESS, 255, 999999999, (byte) 0));
        user.blindnessSent = true;
    }

/*    private static void initializeAlixInterceptor() {
        AlixInterceptor.injectIntoServerPipeline(SERVER_CHANNEL.pipeline());
    }*/

    private static ChannelFuture getServerChannelFuture() {
        try {
            Class<?> mcServerClass = ReflectionUtils.nms2("server.MinecraftServer");
            Object mcServer = mcServerClass.getMethod("getServer").invoke(null);

            Class<?> serverConnectionClass = ReflectionUtils.nms2("server.network.ServerConnection", "server.network.ServerConnectionListener");
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
                    return futures.get(0);//it should contain only one handler
                }
            }
            throw new AlixError("No Server Channel found! - " + Arrays.toString(serverConnectionClass.getDeclaredFields()));
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }

    public static void delayedConfigTeleportExecute(Runnable r, Player p) {
        if (isTeleportDelayed) AlixScheduler.runLaterSync(r, teleportDelay, TimeUnit.MILLISECONDS);
        else r.run();
        AlixUtils.sendMessage(p, delayedTeleportMessage);
    }

    public static void delayedConfigTeleport(Player player, Location loc) {
        if (isTeleportDelayed) {
            AlixScheduler.runLaterSync(() -> MethodProvider.teleportAsync(player, loc), teleportDelay, TimeUnit.MILLISECONDS);
            AlixUtils.sendMessage(player, delayedTeleportMessage);
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

    /*public static ConnectionFilter[] getConnectionFilters() {//set up in the most efficient way
        List<ConnectionFilter> filters = new ArrayList<>();
        //if (ServerPingManager.isRegistered()) filters.add(ServerPingManager.INSTANCE);
        //Moved to AlixChannelHandler
        //if (Main.config.getBoolean("prevent-first-time-join")) filters.add(new ConnectionManager());
        //if (maximumTotalAccounts > 0) filters.add(GeoIPTracker.INSTANCE);
        if (Main.config.getBoolean("anti-vpn")) filters.add(AntiVPN.INSTANCE);
        return filters.toArray(new ConnectionFilter[0]);
    }

    public static ConnectionFilter[] getPremiumConnectionFilters() {//set up in the most efficient way
        List<ConnectionFilter> filters = new ArrayList<>();
        if (maximumTotalAccounts > 0) filters.add(GeoIPTracker.INSTANCE);
        if (Main.config.getBoolean("anti-vpn")) filters.add(AntiVPN.INSTANCE);
        return filters.toArray(new ConnectionFilter[0]);
    }*/

    public static void initExecutors(PluginManager pm) {
        //if (isOfflineExecutorRegistered) {
        pm.registerEvents(new OfflineExecutors(), Main.plugin);
        pm.registerEvents(
                AlixCommonUtils.isValidClass("com.destroystokyo.paper.event.player.PlayerSetSpawnEvent")
                        ? new PaperSpawnExecutors() : new SpigotSpawnExecutors(), Main.plugin);

        //initializeAlixInterceptor();
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
        if (AlixUtils.antibotService || ServerPingManager.isRegistered())
            pm.registerEvents(new ServerPingListener(), Main.plugin);
        /*            if (requireCaptchaVerification) {
         *//*if(ServerEnvironment.getEnvironment() == ServerEnvironment.PAPER) {
                    Main.logInfo("Enabling Async Tab Completion support.");
                    pm.registerEvents(new PaperTabCompletionExecutors(), Main.plugin);
                } else *//*
                //pm.registerEvents(new BukkitTabCompletionExecutors(), Main.plugin);
            }*/

        //Listener listener = PremiumAutoIn.getAutoLoginListener();

        //if (listener != null) pm.registerEvents(listener, Main.plugin);
        //} else pm.registerEvents(new OnlineExecutors(), Main.plugin);
    }

    public static void updateConsoleFilter() {
        Logger logger = ((Logger) LogManager.getRootLogger());
        for (Iterator<Filter> it = logger.getFilters(); it.hasNext(); ) {
            Filter filter = it.next();
            if (filter.getClass().getSimpleName().equals("AlixConsoleFilterHolder")) {
                try {
                    //((AlixConsoleFilterHolder) filter).updateInstance();
                    filter.getClass().getMethod("makeObsolete").invoke(filter);
                    //make sure to not use any 'break;' statements
                } catch (Exception e) {
                    throw new AlixError(e);
                }
            }
        }
        logger.addFilter(AlixConsoleFilterHolder.INSTANCE);
        Main.logDebug(isPluginLanguageEnglish ? "Successfully added AlixConsoleFilter to console!" :
                "Poprawnie zaimplementowano AlixConsoleFilter dla konsoli! ");
    }

    public static void initializeServerPingManager() {
        ServerPingManager.init();
        //JavaScheduler.repeatAsync(ServerPingManager::clear, 10, TimeUnit.MINUTES);
    }

    private static final ByteBuf
            autoLoginMessageBuffer = OutMessagePacketConstructor.constructConst(Messages.autoLoginMessage),
            autoLoginPremiumMessageBuffer = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("auto-login-premium")),
            autoRegisterPremiumMessageBuffer = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("auto-register-premium"));

    //lower version players crash if we send this normally
    /*private static final String
            autoLoginMessage = Messages.autoLoginMessage,
            autoLoginPremiumMessage = Messages.getWithPrefix("auto-login-premium"),
            autoRegisterPremiumMessage = Messages.getWithPrefix("auto-register-premium");*/

    public static UnverifiedUser handleVirtualPlayerJoin(Player p, TemporaryUser user) {
        LoginInfo login = user.getLoginInfo();
        //Already done in the spawn location event
        /*if (login.getVerdict().isAutoLogin() && p.getWorld().getUID().equals(AlixWorld.CAPTCHA_WORLD.getUID())) //tp back if there was an issue with the teleportation beforehand
            OriginalLocationsManager.teleportBack(p);*/
        switch (login.getVerdict()) {//a fast injection based on the login verdict
            case DISALLOWED_NO_DATA:
                GeoIPTracker.addIP(login.getIP());
                return Verifications.add(p, user);
            case DISALLOWED_PASSWORD_RESET://needs to register after a password reset
            case DISALLOWED_LOGIN_REQUIRED://needs to log in
                return Verifications.add(p, user);
            case REGISTER_PREMIUM:
                autoRegisterCommandList.invoke(p);
                addVerifiedUser0(p, user, autoRegisterPremiumMessageBuffer, AuthReason.PREMIUM_AUTO_REGISTER);
                return null;
            case LOGIN_PREMIUM:
                autoLoginCommandList.invoke(p);
                addVerifiedUser0(p, user, autoLoginPremiumMessageBuffer, AuthReason.PREMIUM_AUTO_LOGIN);
                return null;
            case IP_AUTO_LOGIN:
                autoLoginCommandList.invoke(p);
                addVerifiedUser0(p, user, autoLoginMessageBuffer, AuthReason.IP_AUTO_LOGIN);
                return null;
            default:
                user.getChannel().close();
                throw new AlixError("Invalid verdict: " + login.getVerdict());
        }
    }

    private static void addVerifiedUser0(Player p, TemporaryUser user, ByteBuf constJoinMsgBuf, AuthReason authReason) {
        VerifiedUser vUser = UserManager.addVerifiedUser(p, user, verifiedUser -> {
            //verifiedUser.writeAndFlushConstSilently(constJoinMsgBuf);
            /*verifiedUser.reetrooperUser().sendMessage("sexxxx");
            verifiedUser.reetrooperUser().sendPacket(constJoinMsgBuf.duplicate());*/
            verifiedUser.writeAndFlushConstSilently(constJoinMsgBuf);
            //Main.debug("SENT");
        });

        AlixEventInvoker.callOnAuth(authReason, vUser, AlixEvent.ThreadSource.SYNC);
    }

    //returns true if the virtual player is unverified (currently disabled)
    public static void handleVirtualPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        AlixUser user = UserManager.remove(p);

        if (user == null) {
            if (AlixUtils.isFakePlayer(p)) return;
            Main.logError("Could not get any User instance for the player " + p.getName() + " on quit! Report this as an error immediately!");
            return;//true
        }

        if (user instanceof TemporaryUser) {
            return;//something went wrong - don't care
        }

        if (!user.isVerified()) {//unregistered/not logged in user quit handling
            UnverifiedUser u = (UnverifiedUser) user;
            u.uninjectOnQuit();//uninject the user
            event.setQuitMessage(null);//removing the quit message whenever the join message was also removed and never sent
            if (!u.hasAccount()) GeoIPTracker.removeIP(u.getIPAddress());//remove the ip, if it's temporary
            /*if (u.captchaInitialized()) {//quit after captcha was initialized, and the user hasn't registered, occurred
                //if (alixJoinLog) Main.logInfo(quitCaptchaUnverified.format(p.getName(), u.getIPAddress()));
            }// else if (alixJoinLog) Main.logInfo(quitUnverified.format(p.getName(), u.getIPAddress()));*/
            return;//true
        }
        //logged in user quit handling
        VerifiedUser u = (VerifiedUser) user;
        u.onQuit();

        Main.logInfo(playerQuit.format(u.getName(), u.getIPAddress().getHostAddress()));
        //false
    }

/*    public static PacketAffirmator createPacketAffirmatorImpl() {
        if (PacketBlocker.serverboundChatCommandPacketVersion) {
            Main.logInfo("Using the 1.17+ packet affirmator for packet processing.");
            return new NewerVersionedPacketAffirmator();
        }
        Main.logInfo("Using the multi-version packet affirmator for packet processing.");
        return new MultiVersionPacketAffirmator();
    }*/

/*    public static ChannelInjector createChannelInjectorImpl() {
        if (Dependencies.isPacketEventsPresent) {
            Main.logInfo("Using PacketEvents for channel injecting (provided).");
            return new ChannelInjectorPacketEvents();
        }
        *//*if (ServerEnvironment.getEnvironment() == ServerEnvironment.PAPER) {
            Main.logInfo("Using Paper for channel injecting (built-in).");
            return new ChannelInjectorPaper();
        }*//*
        ChannelInjector injector = new ChannelInjectorNMS();
        Main.logInfo("Using " + injector.getProvider() + " for channel injecting (built-in).");
        return injector;
    }*/

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

    private AlixHandler() {
    }
}