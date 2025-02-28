package shadow.systems.commands;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.loc.impl.bukkit.BukkitNamedLocation;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.Messages;
import alix.common.packets.command.CommandsWrapperConstructor;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.multiengine.ban.BukkitBanList;
import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import shadow.Main;
import alix.common.commands.file.AlixCommandInfo;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.systems.commands.alix.impl.AlixCommand;
import shadow.systems.commands.tab.CommandTabCompleterAS;
import shadow.systems.commands.tab.subtypes.*;
import shadow.systems.gui.impl.AccountGUI;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.command.managers.ChatManager;
import shadow.utils.command.managers.PersonalMessageManager;
import shadow.utils.command.tpa.TpaManager;
import shadow.utils.command.tpa.TpaRequest;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.main.file.managers.SpawnFileManager;
import shadow.utils.main.file.managers.WarpFileManager;
import shadow.utils.misc.ReflectionUtils;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.objects.packet.types.verified.VerifiedPacketProcessor;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.users.types.VerifiedUser;

import java.util.Collection;
import java.util.Date;

import static shadow.utils.main.AlixUtils.*;
import static shadow.utils.users.UserManager.getVerifiedUser;

public final class CommandManager {

/*    private static final Map<String, List<String>> polishCommandAliasesMap;

    static {
        polishCommandAliasesMap = new HashMap<>();
        putIntoMap("fly", "latanie", "lataj");
        putIntoMap("rename", "nazwijprzedmiot", "nazwaprzedmiotu", "zmiennazweprzedmiotu");
        putIntoMap("speed", "szybkosc", "predkosc");
        putIntoMap("heal", "ulecz");
        putIntoMap("feed", "najedz", "nasyc");
        putIntoMap("invsee", "zobaczeq", "zobaczekwipunek", "ekwipunek");
        putIntoMap("gamemode", "trybgry");
        putIntoMap("list", "lista");
        putIntoMap("nickname", "nazwa", "zmiennazwe");
        putIntoMap("register", "zarejestruj");
        putIntoMap("login", "loguj", "zaloguj");
        putIntoMap("changepassword", "zmienhaslo");
        putIntoMap("mute", "wycisz");
        putIntoMap("unmute", "odcisz");
        putIntoMap("reply", "odpowiedz");
        putIntoMap("addwarp", "dodajwarp", "stworzwarp");
        putIntoMap("removewarp", "usunwarp");
        putIntoMap("chat", "czat", "czatoff", "czaton");
    }

    private static void putIntoMap(String command, String... aliases) {
        polishCommandAliasesMap.put(command, Arrays.asList(aliases));
    }*/

    public static final String /*antiVpn = Messages.get("anti-vpn"),
            accountLimiter = Messages.get("account-limiter"),
            preventFirstTimeJoin = Messages.get("prevent-first-time-join"),
            pingBeforeJoin = Messages.get("ping-before-join"),*/
            flyingPlayerDisabled = Messages.get("flying-player-disabled"),
            flyingPlayerEnabled = Messages.get("flying-player-enabled"),
            flyingSpeedPlayer = Messages.get("flying-speed-player"),
            flyingSpeedSelf = Messages.get("flying-speed-self"),
            formatChat = Messages.get("format-chat"),
            formatSudo = Messages.get("format-sudo"),
            formatReply = Messages.get("format-reply"),
            formatMsg = Messages.get("format-msg"),
            formatSpawn = Messages.get("format-spawn"),
            formatSetSpawn = Messages.get("format-setspawn"),
            formatUnmute = Messages.get("format-unmute"),
            formatMute = Messages.get("format-mute"),
            formatTpaCancel = Messages.get("format-tpa-cancel"),
            formatTpaDeny = Messages.get("format-tpa-deny"),
            formatTpaAccept = Messages.get("format-tpa-accept"),
            formatTpa = Messages.get("format-tpa"),
            formatRemoveWarp = Messages.get("format-remove-warp"),
            formatAddWarp = Messages.get("format-add-warp"),
            formatWarp = Messages.get("format-warp"),
            formatHomeList = Messages.get("format-home-list"),
            formatHome = Messages.get("format-home"),
            formatRemoveHome = Messages.get("format-remove-home"),
            formatSethome = Messages.get("format-sethome"),
            formatDeopRestricted = Messages.get("format-deop-restricted"),
            formatDeopUnrestricted = Messages.get("format-deop-unrestricted"),
            formatOpRestricted = Messages.get("format-op-restricted"),
            formatOpUnrestricted = Messages.get("format-op-unrestricted"),
            formatTempbanip = Messages.get("format-tempbanip"),
            formatTempban = Messages.get("format-tempban"),
            formatUnban = Messages.get("format-unban"),
            formatUnbanip = Messages.get("format-unbanip"),
    //formatCaptcha = Messages.get("format-captcha"),
    formatChangepassword = Messages.get("format-changepassword"),
            formatLogin = Messages.get("format-login"),
            formatRegister = Messages.get("format-register"),
            formatNicknamePlayer = Messages.get("format-nicknameplayer"),
            formatGamemode = Messages.get("format-gamemode"),
            formatInvsee = Messages.get("format-invsee"),
            formatEnderchest = Messages.get("format-enderchest"),
            formatFeed = Messages.get("format-feed"),
            formatHeal = Messages.get("format-heal"),
            formatSpeed = Messages.get("format-speed"),
            formatFly = Messages.get("format-fly"),
            playerNotFound = Messages.get("player-not-found"),
            errorPlayerNeverJoined = Messages.get("error-player-never-joined"),
            warningPlayerNeverJoined = Messages.get("warning-player-never-joined"),
            playerDataNotFound = Messages.get("player-data-not-found"),
            invalidCommand = Messages.get("invalid-command"),
            sudoedPlayerChat = Messages.get("sudoed-player-chat"),
            commandDoesNotExist = Messages.get("command-does-not-exist"),
            sudoedPlayerCommand = Messages.get("sudoed-player-command"),
            incorrectCommand = Messages.get("incorrect-command"),
            noConversationToReplyTo = Messages.get("no-conversation-to-reply-to"),
            noLongerOnline = Messages.get("no-longer-online"),
            personalMessageSend = Messages.get("personal-message-send"),
            personalMessageReceive = Messages.get("personal-message-receive"),
            spawnTeleport = Messages.get("spawn-teleport"),
            spawnLocationSet = Messages.get("spawn-location-set"),
            successfullyUnmuted = Messages.get("successfully-unmuted"),
            mutedForever = Messages.get("muted-forever"),
            mutedTime = Messages.get("muted-time"),
            tpaRequestAbsent = Messages.get("tpa-request-absent"),
            tpaRequestAbsentFromSelf = Messages.get("tpa-request-absent-from-self"),
            tpaAcceptSelf = Messages.get("tpa-accept-self"),
            tpaAccept = Messages.get("tpa-accept"),
            tpaDenySelf = Messages.get("tpa-deny-self"),
            tpaDeny = Messages.get("tpa-deny"),
            tpaRequestCancel = Messages.get("tpa-request-cancel"),
            tpaOff = Messages.get("tpa-off"),
            tpaOn = Messages.get("tpa-on"),
            tpaFailedTargetHasTpaOff = Messages.get("tpa-failed-target-has-tpa-off"),
            tpaRequestAlreadySent = Messages.get("tpa-request-already-sent"),
            tpaRequestSend = Messages.get("tpa-request-send"),
            tpaRequestReceive = Messages.get("tpa-request-receive"),
            removeWarp = Messages.get("remove-warp"),
            warpAbsent = Messages.get("warp-absent"),
            warpLocationChange = Messages.get("warp-location-change"),
            warpCreate = Messages.get("warp-create"),
            warpTeleport = Messages.get("warp-teleport"),
            listOfHomes = Messages.get("list-of-homes"),
            noHomesSet = Messages.get("no-homes-set"),
            homeTeleportDefault = Messages.get("home-teleport-default"),
            homeTeleportNamed = Messages.get("home-teleport-named"),
            homeAbsent = Messages.get("home-absent"),
            homeNamedAbsent = Messages.get("home-named-absent"),
            successfullyRemoved = Messages.get("successfully-removed"),
            homeDefaultOverwrite = Messages.get("home-default-overwrite"),
            homeNamedOverwrite = Messages.get("home-named-overwrite"),
            homeSet = Messages.get("home-set"),
            commandUnreachable = Messages.get("command-unreachable"),
            invalidName = Messages.get("invalid-name"),
            maxHomesReached = Messages.get("max-homes-reached"),
            playerNotOp = Messages.get("player-not-op"),
            playerWasDeopped = Messages.get("player-was-deopped"),
            playerWasOpped = Messages.get("player-was-opped"),
            incorrectPassword = Messages.get("incorrect-password"),
            playerAlreadyOp = Messages.get("player-already-op"),
            systemUnsureOpPasswordTip = Messages.get("system-unsure-op-password-tip"),
            ipInfoAbsentBan = Messages.get("ip-info-absent-ban"),
            banReasonAbsent = Messages.get("ban-reason-absent"),
            bannedIpForever = Messages.get("banned-ip-forever"),
            bannedIpReason = Messages.get("banned-ip-reason"),
            bannedIpReasonAndTime = Messages.get("banned-ip-reason-and-time"),
            bannedPlayerForever = Messages.get("banned-player-forever"),
            bannedPlayerReason = Messages.get("banned-player-reason"),
            bannedPlayerReasonAndTime = Messages.get("banned-player-reason-and-time"),
            playerNotBanned = Messages.get("player-not-banned"),
            unbannedPlayer = Messages.get("unbanned-player"),
    //ipNotBanned = Messages.get("ip-not-banned"),
    unbannedIp = Messages.get("unbanned-ip"),
    //alreadyLoggedIn = Messages.get("already-logged-in"),
    //captchaAlreadyCompleted = Messages.get("captcha-already-completed"),
    //captchaNotInitialized = Messages.get("captcha-not-initialized"),
    incorrectCaptcha = Messages.get("incorrect-captcha"),
    //loginReminderCommand = Messages.get("login-reminder-command"),
    registerReminderCommand = Messages.get("register-reminder-command"),
            passwordChanged = Messages.get("password-changed"),
            alreadyLoggedInLoginCommand = Messages.get("already-logged-in-login-command"),
            captchaReminderCommand = Messages.get("captcha-reminder-command"),
            passwordRegister = Messages.get("password-register"),
            nicknamePlayerReset = Messages.get("nickname-player-reset"),
            nicknamePlayerSet = Messages.get("nickname-player-set"),
            nicknameReset = Messages.get("nickname-reset"),
            nicknameChangeSelf = Messages.get("nickname-change-self"),
            nonePlayers = Messages.get("none-players"),
            onlinePlayerList = Messages.get("online-player-list"),
            gamemodeSurvivalSelf = Messages.get("gamemode-survival-self"),
            gamemodeCreativeSelf = Messages.get("gamemode-creative-self"),
            gamemodeAdventureSelf = Messages.get("gamemode-adventure-self"),
            gamemodeSpectatorSelf = Messages.get("gamemode-spectator-self"),
            gamemodeSurvivalPlayer = Messages.get("gamemode-survival-player"),
            gamemodeCreativePlayer = Messages.get("gamemode-creative-player"),
            gamemodeAdventurePlayer = Messages.get("gamemode-adventure-player"),
            gamemodeSpectatorPlayer = Messages.get("gamemode-spectator-player"),
            gamemodeSet = Messages.get("gamemode-set"),
            gamemodePlayerSetPlayer = Messages.get("gamemode-player-set-player"),
            feedSelf = Messages.get("feed-self"),
            playerFeed = Messages.get("player-feed"),
            healSelf = Messages.get("heal-self"),
            playerHealed = Messages.get("player-healed"),
            notANumber = Messages.get("not-a-number"),
            walkingSpeedSelf = Messages.get("walking-speed-self"),
            walkingSpeedPlayer = Messages.get("walking-speed-player"),
            speedInvalidArg = Messages.get("speed-invalid-arg"),
            itemAbsentDuringRenaming = Messages.get("item-absent-during-renaming"),
            itemNameReset = Messages.get("item-name-reset"),
            renamedItem = Messages.get("renamed-item"),
            flyingEnabled = Messages.get("flying-enabled"),
            flyingDisabled = Messages.get("flying-disabled");

    private static final String FALLBACK_PREFIX = Main.plugin.getName().toLowerCase();

    private static void registerCommand(String commandLabel, CommandExecutor executor) throws RuntimeException {
        registerCommand(commandLabel, executor, (TabCompleter) null);
    }

    private static void registerCommand(String commandLabel, CommandExecutor executor, TabCompleter completer) throws RuntimeException {
        registerCommand(commandLabel, executor, completer, FALLBACK_PREFIX + "." + commandLabel, false);
    }

    private static void registerCommand(String commandLabel, CommandExecutor executor, String permission) throws RuntimeException {
        registerCommand(commandLabel, executor, null, permission, false);
    }

    private static void registerCommand(String commandLabel, CommandExecutor executor, TabCompleter completer, String permission) throws RuntimeException {
        registerCommand(commandLabel, executor, completer, permission, false);
    }

    private static void registerPermissionlessCommandForcibly(String commandLabel, CommandExecutor executor) throws RuntimeException {
        registerCommand(commandLabel, executor, null, null, true);
    }

    private static void registerCommandForcibly(String commandLabel, CommandExecutor executor, TabCompleter completer, String permission) throws RuntimeException {
        registerCommand(commandLabel, executor, completer, permission, true);
    }

    private static void registerCommand(String commandLabel, CommandExecutor executor, TabCompleter completer, String permission, boolean forceRegister) throws RuntimeException {
        AlixCommandInfo info = AlixCommandManager.getCommand(commandLabel);

        if (info == null)
            throw new RuntimeException("The given command " + commandLabel + " does not have an Alix implementation, and therefore cannot be registered!");

        CommandMap map = ReflectionUtils.commandMap;
        Command command = map.getCommand(commandLabel);

        if (!info.isRegistered() && forceRegister)
            Main.logWarning("Unable to prevent the registering of a System command - " + info.getCommand());

        if (forceRegister || info.isRegistered() && (overrideExistingCommands || command == null)) {
            AlixCommand alix = new AlixCommand(info, permission, executor, completer);
            //Main.logInfo("Arix: " + info.getCommand() + " " + map.register(FALLBACK_PREFIX, alix));
            ReflectionUtils.serverKnownCommands.put(commandLabel, alix);
            ReflectionUtils.serverKnownCommands.put(FALLBACK_PREFIX + ":" + commandLabel, alix);
            if (info.hasAliases()) {
                for (String alias : info.getAliases()) {
                    ReflectionUtils.serverKnownCommands.put(alias, alix);
                    ReflectionUtils.serverKnownCommands.put(FALLBACK_PREFIX + ":" + alias, alix);
                }
            }
            return;
        }
        if (info.isFallbackRegistered()) {
            AlixCommand alix = new AlixCommand(commandLabel, permission, executor, completer);//creates a prefix fallback command
            ReflectionUtils.serverKnownCommands.put(FALLBACK_PREFIX + ":" + commandLabel, alix);//registers it directly into the map
        }
    }
        /*if (!info.isRegistered() && !forceRegister) {
            AlixCommand alix = new AlixCommand(commandLabel, permission, executor, completer);//creates a prefix fallback command
            ReflectionUtils.serverKnownCommands.put(FALLBACK_PREFIX + ":" + commandLabel, alix);//registers it directly into the map
            return;
        }*/


    //throw new RuntimeException("The given command " + commandLabel + " does not exist and therefore cannot be registered!");

    //CommandExecutor e = command.getExecutor();

    //Main.logInfo(commandLabel + " c1 " + e.toString());

/*        if(!forceRegister) {
            if(JavaHandler.borrowCommandExecutorIfRegistered(commandLabel)) return;
        }*/

        /*if (!alix.isRegistered()) {
            if (forceRegister) {
                Main.logWarning("Unable to unregister a force-register command - " + commandLabel + "!");
            } else {
                command.unregister(map);
                AlixHandler.findAndSetFallbackCommandExecutor(map, command, commandLabel);
                return;
            }
        }

        if (forceRegister || e instanceof JavaPlugin && e.equals(Main.plugin)) {

            //Main.logInfo(commandLabel + " c2 " + ((JavaPlugin)e).getName());

            command.setExecutor(executor);

            if (alix.hasAliases()) {
                List<String> list = alix.createAliasesList();
                if (command.isRegistered()) {
                    command.unregister(map);
                    //if(!command.unregister(map)) throw new RuntimeException("Unable to unregister ");
                }
                command.setAliases(list);
            *//*for(String s : list) {
                ReflectionUtils.commandMap.register(s, command);
            }*//*
            }

            map.register(Main.plugin.getName().toLowerCase(), command);
        }*/


    public static void register() {
        registerDefaultCommands();
        ReflectionUtils.reloadCommands();
    }
    //registerCompleters(i);
        /*if (isPluginLanguageEnglish) registerEnglishCommands(i);
        else registerPolishCommands(i);*/
    //polishCommandAliasesMap.clear();


/*    private static void registerCompleters(JavaPlugin i) {
        i.getCommand("as").setTabCompleter(new CommandTabCompleterAS());
        i.getCommand("deop").setTabCompleter(new OperatorCommandTabCompleter());
        TabCompleter warpCommandTabCompleter = new WarpCommandTabCompleter();
        i.getCommand("warp").setTabCompleter(warpCommandTabCompleter);
        i.getCommand("removewarp").setTabCompleter(warpCommandTabCompleter);
        i.getCommand("unban").setTabCompleter(new NameBanCommandTabCompleter());
        i.getCommand("unbanip").setTabCompleter(new IPBanCommandTabCompleter());
        i.getCommand("chat").setTabCompleter(new ChatCommandTabCompleter());
        TabCompleter homeCommandTabCompleter = new HomeCommandTabCompleter();
        i.getCommand("home").setTabCompleter(homeCommandTabCompleter);
        i.getCommand("removehome").setTabCompleter(homeCommandTabCompleter);
    }*/

    private static void registerDefaultCommands() {
        try {
            registerCommandForcibly("alixsystem", new AdminAlixCommands(), new CommandTabCompleterAS(), "alixsystem.admin");
            registerCommand("fly", new FlyCommand());
            registerCommand("rename", new ItemRenameCommand());
            registerCommand("speed", new SpeedCommand());
            //registerCommand("vanish", new VanishCommand());
            registerCommand("heal", new HealCommand());
            registerCommand("feed", new FeedCommand());
            registerCommand("enderchest", new EnderChestCommand());
            registerCommand("invsee", new InventoryViewCommand());
            registerCommand("gamemode", new GamemodeSwitchCommand());
            registerCommand("list", new OnlinePlayersListCommand());
            registerCommand("nickname", new NickNameCommand());

            //if (requireCaptchaVerification)
            //registerPermissionlessCommandForcibly("captcha", new CaptchaVerifyCommand());

            /**
             * Logic moved to {@link CommandsWrapperConstructor}
             **/
            //registerPermissionlessCommandForcibly("register", new RegisterCommand());
            //registerPermissionlessCommandForcibly("login", new LoginCommand());

            registerPermissionlessCommandForcibly("changepassword", new PasswordChangeCommand());
            registerPermissionlessCommandForcibly("account", new AccountSettingsCommand());
            registerPermissionlessCommandForcibly("premium", new PremiumCommand());

            registerCommand("unban", new UnbanCommand(), new NameBanCommandTabCompleter());
            registerCommand("unbanip", new UnbanIPCommand(), new IPBanCommandTabCompleter());
            registerCommand("tempban", new TemporaryBanCommand());
            registerCommand("tempbanip", new TemporaryIPBanCommand());
            registerCommand("op", new OperatorSetCommand(), new OpCommandTabCompleter());
            registerCommand("deop", new OperatorUnsetCommand(), new DeopCommandTabCompleter());
            TabCompleter warpCommandTabCompleter = new WarpCommandTabCompleter();
            registerCommand("warp", new WarpTeleportCommand(), warpCommandTabCompleter);
            registerCommand("addwarp", new WarpCreateCommand(), "alixsystem.admin.warp");
            registerCommand("removewarp", new WarpRemoveCommand(), warpCommandTabCompleter, "alixsystem.admin.warp");
            TabCompleter homeCommandTabCompleted = new HomeCommandTabCompleter();
            registerCommand("removehome", new HomeRemoveCommand(), homeCommandTabCompleted, "alixsystem.home");
            registerCommand("sethome", new HomeSetCommand(), "alixsystem.home");
            registerCommand("home", new HomeTeleportCommand(), homeCommandTabCompleted, "alixsystem.home");
            registerCommand("homelist", new HomeListCommand(), "alixsystem.home");
            registerCommand("tpa", new TeleportAskCommand(), "alixsystem.tpa");
            registerCommand("tpaccept", new TeleportAcceptCommand(), "alixsystem.tpa");
            registerCommand("tpadeny", new TeleportDenyCommand(), "alixsystem.tpa");
            registerCommand("tpacancel", new TeleportCancelCommand(), "alixsystem.tpa");
            registerCommand("tpaon", new TeleportAskReceiveSetOnCommand(), "alixsystem.tpa");
            registerCommand("tpaoff", new TeleportAskReceiveSetOffCommand(), "alixsystem.tpa");
            registerCommand("mute", new MuteCommand());
            registerCommand("unmute", new UnmuteCommand());
            registerCommand("setspawn", new SpawnSetCommand());
            registerCommand("spawn", new SpawnTeleportCommand());
            registerCommand("msg", new DirectMessageCommand());
            registerCommand("reply", new ReplyCommand());
            registerCommand("sudo", new SudoCommand());
            registerCommand("chat", new UniversalChatStatusSetCommand(), new ChatCommandTabCompleter(), "alixsystem.admin.chat");
            registerCommand("chatclear", new UniversalChatClearCommand(), "alixsystem.admin.chatclear");
            Main.logInfo("All commands have been successfully registered.");
        } catch (Exception e) {
            Main.logWarning("Commands could not have been registered because of an error!");
            e.printStackTrace();
        }
    }

    /*private static void registerPolishCommands(Main i) {
        try {
            registerCommand("as", new CommandsPL());
            registerCommand("fly", new LatanieKomenda());
            registerCommand("rename", new NazwijPrzedmiotKomenda());
            registerCommand("speed", new PredkoscKomenda());
            //registerCommand("vanish", new ZniknijKomenda());
            registerCommand("heal", new UleczKomenda());
            registerCommand("feed", new NajedzKomenda());
            registerCommand("enderchest", new SkrzyniaKresuKomenda());
            registerCommand("invsee", new ZobaczEkwipunekKomenda());
            registerCommand("gamemode", new ZmianaTrybuGryKomenda());
            registerCommand("list", new ListaGraczyOnlineKomenda());
            registerCommand("nickname", new ZmianaNazwyKomenda());
            if (isOfflineExecutorRegistered) {
                if (requireCaptchaVerification) {
                    registerCommand("captcha", new UkonczCaptcheKomenda());
                }
                registerCommand("register", new ZarajestrujSieKomenda());
                registerCommand("login", new LogowanieSieKomenda());
                registerCommand("changepassword", new ZmianaHaslaKomenda());
            }
            registerCommand("unban", new WybaczenieKomenda());
            registerCommand("unbanip", new WybaczenieIPKomenda());
            registerCommand("tempban", new TymczasowyBanKomenda());
            registerCommand("tempbanip", new TymczasowyBanIPKomenda());
            registerCommand("op", new UstawienieOperatoraKomenda());
            registerCommand("deop", new UsuniecieOperatoraKomenda());
            registerCommand("warp", new WarpTeleportacjaKomenda());
            registerCommand("addwarp", new WarpStworzKomenda());
            registerCommand("removewarp", new WarpUsunKomenda());
            registerCommand("removehome", new UsuniecieDomuKomenda());
            registerCommand("sethome", new UstawienieDomuKomenda());
            registerCommand("home", new TeleportacjaDoDomuKomenda());
            registerCommand("homes", new ListaDomowKomenda());
            registerCommand("tpa", new ZapytanieTeleportacjiKomenda());
            registerCommand("tpaccept", new AkceptacjaTeleportacjiKomenda());
            registerCommand("tpadeny", new OdrzucenieTeleportacjiKomenda());
            registerCommand("tpacancel", new CofniecieTeleportacjiKomenda());
            registerCommand("tpaon", new UstawianieOnPrzyjomowaniaZapytanTeleportacji());
            registerCommand("tpaoff", new UstawianieOffPrzyjomowaniaZapytanTeleportacji());
            registerCommand("mute", new WyciszenieKomenda());
            registerCommand("unmute", new OdciszenieKomenda());
            registerCommand("setspawn", new UstawienieSpawnaKomenda());
            registerCommand("spawn", new TeleportacjaDoSpawnaKomenda());
            registerCommand("msg", new OsobistaWiadomoscKomenda());
            registerCommand("reply", new OdpowiedzKomenda());
            registerCommand("sudo", new SudoKomenda());
            i.logConsoleInfo("Wszystkie komendy zostały poprawnie zarejestrowane.");
        } catch (Exception e) {
            i.logConsoleWarning("Nie można było zarejestrować komend za względu na error!");
            e.printStackTrace();
        }
    }*/

    private static final class UniversalChatStatusSetCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            switch (args.length) {
                case 0:
                    switch (label) {
                        case "chaton":
                        case "czaton":
                            AlixHandler.handleChatTurnOn(sender);
                            return true;
                        case "chatoff":
                        case "czatoff":
                            AlixHandler.handleChatTurnOff(sender);
                            return true;
                    }
                    break;
                case 1:
                    switch (args[0]) {
                        case "clear":
                            ChatManager.clearChat(sender);
                            return true;
                        case "on":
                            AlixHandler.handleChatTurnOn(sender);
                            return true;
                        case "off":
                            AlixHandler.handleChatTurnOff(sender);
                            return true;
                    }
                    break;
            }
            sendMessage(sender, formatChat);
            return false;
        }
    }

    private static final class UniversalChatClearCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            ChatManager.clearChat(sender);
/*            new AutoCancellingRunnable(25, TimeUnit.MILLISECONDS, true, 120) {

                @Override
                public void run() {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendRawMessage("");
                    }
                    super.checkForCancel();
                }

                @Override
                public void cancel() {
                    broadcast(isPluginLanguageEnglish ? "Chat has been cleared by &c" + sender.getName() :
                            "Czat został wyczyszczony przez &c" + sender.getName());
                    super.cancel();
                }
            };*/
            return false;
        }
    }

    private static final class SudoCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length > 1) {
                String arg1 = args[0];
                Player p = Bukkit.getPlayerExact(arg1);
                if (p == null) {
                    sendMessage(sender, playerNotFound, arg1);
                    return false;
                }
                String arg2 = args[1];
                if (startsWith(arg2, "c:", "chat:", "t:", "type:")) {
                    String toChat = getAllUntilCharFoundCharIncluded(mergeWithSpaces(args), ':'); //mergeWithSpacesAndSkip(args, 2);
                    sendMessage(sender, sudoedPlayerChat, arg1);
                    p.chat(toChat);
                    return true;
                }
                String toCommandSend = unslashify(mergeWithSpacesAndSkip(args, 1));
                if (!commandExists(split(toCommandSend, ' ')[0])) {
                    sendMessage(sender, commandDoesNotExist);
                    return false;
                }

                if (p.performCommand(toCommandSend)) {//success
                    sendMessage(sender, sudoedPlayerCommand, arg1, toCommandSend);
                    return true;
                }
                sendMessage(sender, incorrectCommand);
                return false;
            }
            sendMessage(sender, formatSudo);
            return false;
        }
    }

    private static final class ReplyCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length != 0) {
                String replier = sender.getName();
                String originalSender = PersonalMessageManager.get(replier);

                if (originalSender == null) {
                    sendMessage(sender, noConversationToReplyTo);
                    return false;
                }
                Player p = Bukkit.getPlayerExact(originalSender);
                if (p == null) {
                    sendMessage(sender, noLongerOnline, originalSender);
                    return false;
                }
                String message = mergeWithSpaces(args);

                sender.sendMessage(AlixFormatter.formatSingle(personalMessageSend + message, originalSender));
                p.sendMessage(AlixFormatter.formatSingle(personalMessageReceive + message, replier));

                PersonalMessageManager.add(replier, originalSender); //For /msg shortcut
                return true;
            }
            sendMessage(sender, formatReply);
            return false;
        }
    }

    private static final class DirectMessageCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length > 1) {
                String arg1 = args[0];

                Player p = Bukkit.getPlayerExact(arg1);

                if (p == null) {
                    sendMessage(sender, playerNotFound, arg1);
                    return false;
                }

                String receiverName = p.getName();
                String senderName = sender.getName();

                String message = mergeWithSpacesAndSkip(args, 1);

                sender.sendMessage(AlixFormatter.formatSingle(personalMessageSend + message, receiverName));
                p.sendMessage(AlixFormatter.formatSingle(personalMessageReceive + message, senderName));

                PersonalMessageManager.add(senderName, receiverName);
                return true;
            }
            sendMessage(sender, formatMsg);
            return false;
        }
    }

    private static final class SpawnTeleportCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 0) {
                Player p = (Player) sender;
                AlixHandler.delayedConfigTeleportExecute(() -> {
                    MethodProvider.teleportAsync(p, SpawnFileManager.getSpawnLocation());
                    sendMessage(sender, spawnTeleport);
                }, p);
                return true;
            }
            sendMessage(sender, formatSpawn);
            return false;
        }
    }

    private static final class SpawnSetCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 0) {
                Player p = (Player) sender;
                SpawnFileManager.set(p.getLocation());
                sendMessage(sender, spawnLocationSet);
                return true;
            }
            sendMessage(sender, formatSetSpawn);
            return false;
        }
    }

    private static final class UnmuteCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String arg1 = args[0];/*
                OfflinePlayer p = getOfflinePlayer(arg1);
                if (p == null) {
                    sendMessage(sender, errorPlayerNeverJoined, arg1);
                    return false;
                }
                String name = p.getName();*/
                PersistentUserData data = UserFileManager.get(arg1);
                if (data == null) {
                    sendMessage(sender, playerDataNotFound, arg1);
                    return false;
                }
                data.setMutedUntil(0);
                sendMessage(sender, successfullyUnmuted, arg1);
                return true;
            }
            sendMessage(sender, formatUnmute);
            return false;
        }
    }

    private static final class MuteCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            switch (args.length) {
                case 1: {
                    String arg1 = args[0];/*
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null) {
                        sendMessage(sender, errorPlayerNeverJoined, arg1);
                        return false;
                    }
                    String name = p.getName();*/
                    PersistentUserData data = UserFileManager.get(arg1);
                    if (data == null) {
                        sendMessage(sender, playerDataNotFound, arg1);
                        return false;
                    }
                    sendMessage(sender, mutedForever, arg1);
                    data.setMutedUntil(0x9b627f782d8L); //10677959230168L as for 1677959230168L, which is the time this code was written in,
                    //but in hex to look fancy :>
                }
                return true;
                case 2: {
                    String arg1 = args[0];/*
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null) {
                        sendMessage(sender, errorPlayerNeverJoined, arg1);
                        return false;
                    }
                    String name = p.getName();*/
                    PersistentUserData data = UserFileManager.get(arg1);
                    if (data == null) {
                        sendMessage(sender, playerDataNotFound, arg1);
                        return false;
                    }
                    long time = getProcessedTimePlusNow(args[1]);
                    data.setMutedUntil(time);
                    sendMessage(sender, mutedTime, arg1, getFormattedDate(new Date(time)));
                }
                return true;
            }
            sendMessage(sender, formatMute);
            return false;
        }
    }

    private static final class TeleportAskReceiveSetOffCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            UserManager.getVerifiedUser((Player) sender).setCanReceiveTeleportRequests(false);
            sendMessage(sender, tpaOff);
            return true;
        }
    }

    private static final class TeleportAskReceiveSetOnCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            UserManager.getVerifiedUser((Player) sender).setCanReceiveTeleportRequests(true);
            sendMessage(sender, tpaOn);
            return true;
        }
    }

    private static final class TeleportCancelCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 0) {
                Player p = (Player) sender;
                TpaRequest request = TpaManager.getRequest(p.getName());
                if (request == null) {
                    sendMessage(p, tpaRequestAbsentFromSelf);
                    return false;
                }
                request.remove();
                sendMessage(p, tpaRequestCancel);
                return true;
            }
            sendMessage(sender, formatTpaCancel);
            return false;
        }
    }

    private static final class TeleportDenyCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length != 0) {
                Player p = (Player) sender;
                String arg1 = args[0];
                Player p2 = Bukkit.getPlayerExact(arg1);
                if (p2 == null) {
                    sendMessage(sender, playerNotFound, arg1);
                    return false;
                }
                String name = p2.getName();
                TpaRequest request = TpaManager.getRequest(name);
                if (request == null) {
                    sendMessage(p, tpaRequestAbsent, name);
                    return false;
                }
                request.remove();
                sendMessage(p, tpaDenySelf, name);
                sendMessage(p2, tpaDeny);
                return true;
            }
            sendMessage(sender, formatTpaDeny);
            return false;
        }
    }

    private static final class TeleportAcceptCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length != 0) {
                Player acceptor = (Player) sender;
                String arg1 = args[0];
                Player theTeleported = Bukkit.getPlayerExact(arg1);
                if (theTeleported == null) {
                    sendMessage(sender, playerNotFound, arg1);
                    return false;
                }
                String name = theTeleported.getName();
                TpaRequest request = TpaManager.getRequest(name);
                if (request == null) {
                    sendMessage(theTeleported, tpaRequestAbsent, theTeleported.getName());
                    return false;
                }
                request.accept();
                sendMessage(acceptor, tpaAcceptSelf, name);
                sendMessage(theTeleported, tpaAccept);
                return true;
            }
            sendMessage(sender, formatTpaAccept);
            return false;
        }
    }

    private static final class TeleportAskCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length != 0) {
                Player p = (Player) sender;
                String arg1 = args[0];
                Player p2 = Bukkit.getPlayerExact(arg1);
                if (p2 == null) {
                    sendMessage(sender, playerNotFound, arg1);
                    return false;
                }
                if (TpaManager.hasRequestsOff(p2)) {
                    sendMessage(sender, tpaFailedTargetHasTpaOff, arg1);
                    return false;
                }
                String name = p.getName();
                TpaRequest request = TpaManager.getRequest(name);
                if (request != null) {
                    sendMessage(p, tpaRequestAlreadySent, request.getSentToName());
                    return false;
                }
                TpaManager.addRequest(name, p, p2);
                sendMessage(p, tpaRequestSend, p2.getName(), TpaRequest.tpaAutoExpire);
                sendMessage(p2, tpaRequestReceive, name, TpaRequest.tpaAutoExpire);
                return true;
            }
            sendMessage(sender, formatTpa);
            return false;
        }
    }

    private static final class WarpRemoveCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String name = args[0];
                if (WarpFileManager.remove(name) != null) {
                    sendMessage(sender, removeWarp, name);
                    return true;
                }
                sendMessage(sender, warpAbsent, name);
                return false;
            }
            sendMessage(sender, formatRemoveWarp);
            return false;
        }
    }

    private static final class WarpCreateCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 1) {
                Player p = (Player) sender;
                Location l = p.getLocation();
                String name = args[0];
                BukkitNamedLocation warp = WarpFileManager.get(name);
                if (warp != null) {
                    WarpFileManager.replace(new BukkitNamedLocation(l, warp.getName()));
                    sendMessage(sender, warpLocationChange, name);
                    return true;
                }
                WarpFileManager.add(new BukkitNamedLocation(l, name));
                sendMessage(sender, warpCreate, name);
                return true;
            }
            sendMessage(sender, formatAddWarp);
            return false;
        }
    }

    private static final class WarpTeleportCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 1) {
                Player p = (Player) sender;
                String name = args[0];
                BukkitNamedLocation warp = WarpFileManager.get(name);
                if (warp != null) {
                    AlixHandler.delayedConfigTeleportExecute(() -> {
                        warp.teleport(p);
                        sendMessage(sender, warpTeleport, name);
                    }, p);
                    return true;
                }
                sendMessage(sender, warpAbsent, name);
                return false;
            }
            sendMessage(sender, formatWarp);
            return false;
        }
    }

    private static final class HomeListCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 0) {
                Player p = (Player) sender;
                BukkitNamedLocation[] homes = getVerifiedUser(p).getHomes().array();
                if (homes.length != 0) {
                    sendMessage(sender, listOfHomes);
                    sendMessage(sender, "");
                    for (BukkitNamedLocation h : homes) sendMessage(sender, h.toUserReadable());
                    sendMessage(sender, "");
                    return true;
                }
                sendMessage(sender, noHomesSet);
                return true;
            }
            sendMessage(sender, formatHomeList);
            return false;
        }
    }

    private static final class HomeTeleportCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            VerifiedUser u = getVerifiedUser(p);
            switch (l) {
                case 0: {
                    BukkitNamedLocation home = u.getHomes().getByName("default");
                    if (home != null) {
                        AlixHandler.delayedConfigTeleportExecute(() -> {
                            home.teleport(p);
                            sendMessage(sender, homeTeleportDefault);
                        }, p);
                        return true;
                    }
                    sendMessage(sender, homeAbsent);
                    return false;
                }
                case 1: {
                    String name = args[0];
                    BukkitNamedLocation home = u.getHomes().getByName(name);
                    if (home != null) {
                        AlixHandler.delayedConfigTeleportExecute(() -> {
                            home.teleport(p);
                            sendMessage(sender, homeTeleportNamed, name);
                        }, p);
                        return true;
                    }
                    sendMessage(sender, homeNamedAbsent, name);
                    return false;
                }
                default:
                    sendMessage(sender, formatHome);
                    return false;
            }
        }
    }

    private static final class HomeRemoveCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                VerifiedUser u = getVerifiedUser(p);
                String name = args[0];
                int i = u.getHomes().indexOf(name);
                if (i != -1) {
                    u.getHomes().removeHome(i);
                    sendMessage(sender, successfullyRemoved, name);
                    return true;
                }
                sendMessage(sender, homeNamedAbsent, name);
                return false;
            }
            sendMessage(sender, formatRemoveHome);
            return false;
        }
    }

    private static final class HomeSetCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            Location loc = p.getLocation();
            VerifiedUser u = getVerifiedUser(p);
            int current = u.getHomes().array().length;
            short max = u.getMaxHomes();
            switch (l) {
                case 0:
                    if (current < max) {
                        int i = u.getHomes().indexOf("default");
                        if (i != -1) {
                            u.getHomes().setHome(i, new BukkitNamedLocation(loc, "default"));
                            sendMessage(sender, homeDefaultOverwrite);
                        } else {
                            u.getHomes().addHome(new BukkitNamedLocation(loc, "default"));
                            sendMessage(sender, homeSet);
                        }
                        return true;
                    }
                    sendMessage(sender, maxHomesReached, max);
                    return false;
                case 1:
                    String name = args[0];
                    if (current < max) {
                        if (name.equals("default")) {
                            sendMessage(sender, invalidName);
                            return false;
                        }
                        String reason = getInvalidityReason(name, true);
                        if (reason != null) {
                            sendMessage(sender, reason);
                            return false;
                        }
                        int i = u.getHomes().indexOf(name);
                        if (i != -1) {
                            u.getHomes().setHome(i, new BukkitNamedLocation(loc, name));
                            sendMessage(sender, homeNamedOverwrite, name);
                        } else {
                            u.getHomes().addHome(new BukkitNamedLocation(loc, name));
                            sendMessage(sender, homeSet);
                        }
                        return true;
                    }
                    sendMessage(sender, maxHomesReached, max);
                    return false;
                default:
                    sendMessage(sender, formatSethome);
                    return false;
            }
        }
    }

    private static final class OperatorUnsetCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            boolean consoleSender = sender instanceof ConsoleCommandSender;
            int l = args.length;
            if (l == 1 && (!isOperatorCommandRestricted || consoleSender)) {
                String arg1 = args[0];
                OfflinePlayer p = getOfflinePlayer(arg1);
                if (p == null) {
                    sendMessage(sender, warningPlayerNeverJoined, arg1);
                    AlixHandler.handleOperatorUnset(sender, arg1);
                    return false;
                }
                if (!p.isOp()) {
                    sendMessage(sender, playerNotOp, p.getName());
                    return false;
                }
                p.setOp(false);
                broadcastColorizedToPermitted(AlixFormatter.format(playerWasDeopped, p.getName(), sender.getName()));
                return true;
            } else if (l == 2 && isOperatorCommandRestricted) {
                String arg1 = args[0];
                String arg2 = args[1];
                OfflinePlayer p = getOfflinePlayer(arg1);
                boolean correctPassword = arg2.equals(operatorCommandPassword);
                if (p == null) {
                    if (!correctPassword) {
                        sendMessage(sender, incorrectPassword);
                        return false;
                    }
                    sendMessage(sender, warningPlayerNeverJoined, arg1);
                    AlixHandler.handleOperatorUnset(sender, arg1);
                    return false;
                }
                if (correctPassword || consoleSender) {
                    if (!p.isOp()) {
                        sendMessage(sender, playerNotOp);
                        return false;
                    }
                    p.setOp(false);
                    broadcastColorizedToPermitted(AlixFormatter.format(playerWasDeopped, p.getName(), sender.getName()));
                } else sendMessage(sender, incorrectPassword);
                return true;
            } else
                sendMessage(sender, isOperatorCommandRestricted ? formatDeopRestricted : formatDeopUnrestricted);
            if (isOperatorCommandRestricted)
                sendMessage(sender, systemUnsureOpPasswordTip);
            return false;
        }
    }

    private static final class OperatorSetCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            boolean consoleSender = sender instanceof ConsoleCommandSender;
            int l = args.length;
            if (l == 1 && (!isOperatorCommandRestricted || consoleSender)) {
                String arg1 = args[0];
                OfflinePlayer p = getOfflinePlayer(arg1);
                if (p == null) {
                    sendMessage(sender, warningPlayerNeverJoined, arg1);
                    broadcastColorizedToPermitted(AlixFormatter.format(playerWasOpped, arg1, sender.getName()));
                    AlixHandler.handleOperatorSet(sender, arg1);
                    return false;
                }
                if (p.isOp()) {
                    sendMessage(sender, playerAlreadyOp, p.getName());
                    return false;
                }
                p.setOp(true);
                broadcastColorizedToPermitted(AlixFormatter.format(playerWasOpped, p.getName(), sender.getName()));
                return true;
            } else if (l == 2 && isOperatorCommandRestricted) {
                String arg1 = args[0];
                String arg2 = args[1];
                OfflinePlayer p = getOfflinePlayer(arg1);
                boolean correctPassword = arg2.equals(operatorCommandPassword);
                if (p == null) {
                    if (!correctPassword) {
                        sendMessage(sender, incorrectPassword);
                        return false;
                    }
                    sendMessage(sender, warningPlayerNeverJoined, arg1);

                    broadcastColorizedToPermitted(AlixFormatter.format(playerWasOpped, arg1, sender.getName()));
                    AlixHandler.handleOperatorSet(sender, arg1);
                    return false;
                }
                if (correctPassword || consoleSender) {
                    if (p.isOp()) {
                        sendMessage(sender, playerAlreadyOp, p.getName());
                        return false;
                    }
                    p.setOp(true);
                    broadcastColorizedToPermitted(AlixFormatter.format(playerWasOpped, p.getName(), sender.getName()));
                } else sendMessage(sender, incorrectPassword);
                return true;
            } else {
                sendMessage(sender, isOperatorCommandRestricted ? formatOpRestricted : formatOpUnrestricted);
                if (isOperatorCommandRestricted)
                    sendMessage(sender, systemUnsureOpPasswordTip);
            }
            return false;
        }
    }

    private static final class TemporaryIPBanCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            switch (l) {
                case 1: {
                    String arg1 = args[0];
                    boolean valid = isValidIP(arg1);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null && !valid) {
                        sendMessage(sender, errorPlayerNeverJoined, arg1);
                        return false;
                    }

                    PersistentUserData data = UserFileManager.get(p.getName());
                    String address = valid ? arg1 : data == null ? "0" : data.getSavedIP().getHostAddress();

                    if (address.equals("0")) {
                        sendMessage(sender, ipInfoAbsentBan, p.getName());
                        return false;
                    }
                    ban(address, banReasonAbsent, null, true, sender.getName());
                    broadcastColorizedToPermitted(AlixFormatter.format(bannedIpForever, address));
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String reason = translateColors(args[1]);
                    boolean valid = isValidIP(arg1);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null && !valid) {
                        sendMessage(sender, errorPlayerNeverJoined, arg1);
                        return false;
                    }

                    PersistentUserData data = UserFileManager.get(p.getName());
                    String address = valid ? arg1 : data == null ? "0" : data.getSavedIP().getHostAddress();

                    if (address.equals("0")) {
                        sendMessage(sender, ipInfoAbsentBan, p.getName());
                        return false;
                    }
                    ban(address, reason, null, true, sender.getName());
                    broadcastColorizedToPermitted(AlixFormatter.format(bannedIpReason, address, reason));
                    return true;
                }
                case 3: {
                    String arg1 = args[0];
                    String reason = translateColors(args[1]);
                    String arg3 = args[2];
                    boolean valid = isValidIP(arg1);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null && !valid) {
                        sendMessage(sender, errorPlayerNeverJoined, arg1);
                        return false;
                    }
                    PersistentUserData data = UserFileManager.get(p.getName());
                    String address = valid ? arg1 : data == null ? "0" : data.getSavedIP().getHostAddress();

                    if (address.equals("0")) {
                        sendMessage(sender, ipInfoAbsentBan, p.getName());
                        return false;
                    }
                    Date date = getProcessedDate(arg3);
                    ban(address, reason, date, true, sender.getName());
                    broadcastColorizedToPermitted(AlixFormatter.format(bannedIpReasonAndTime, address, getFormattedDate(date), reason));
                    break;
                }
                default:
                    sendMessage(sender, formatTempbanip);
                    break;
            }
            return false;
        }
    }

    private static final class TemporaryBanCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            switch (l) {
                case 1: {
                    String arg1 = args[0];
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    boolean found = p != null;
                    if (!found)
                        sendMessage(sender, warningPlayerNeverJoined, arg1);
                    String name = found ? p.getName() : arg1;
                    ban(name, banReasonAbsent, null, false, sender.getName());
                    broadcastColorizedToPermitted(AlixFormatter.format(bannedPlayerForever, name));
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String reason = translateColors(args[1]);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    boolean found = p != null;
                    if (!found)
                        sendMessage(sender, warningPlayerNeverJoined, arg1);
                    String name = found ? p.getName() : arg1;
                    assert name != null;
                    ban(name, reason, null, false, sender.getName());
                    broadcastColorizedToPermitted(AlixFormatter.format(bannedPlayerReason, name, reason));
                    return true;
                }
                case 3: {
                    String arg1 = args[0];
                    String arg3 = args[2];
                    String reason = translateColors(args[1]);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    boolean found = p != null;
                    if (!found)
                        sendMessage(sender, warningPlayerNeverJoined, arg1);
                    String name = found ? p.getName() : arg1;
                    Date date = getProcessedDate(arg3);
                    assert name != null;
                    ban(name, reason, date, false, sender.getName());
                    broadcastColorizedToPermitted(AlixFormatter.format(bannedPlayerReasonAndTime, name, getFormattedDate(date), reason));
                    break;
                }
                default:
                    sendMessage(sender, formatTempban);
                    break;
            }
            return false;
        }
    }

    private static final class UnbanCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String arg1 = args[0];
                if (!BukkitBanList.NAME.isBanned(arg1)) {
                    sendMessage(sender, playerNotBanned, arg1);
                    return false;
                }
                unban(arg1, false);
                sendMessage(sender, unbannedPlayer, arg1);
                return true;
            }
            sendMessage(sender, formatUnban);
            return false;
        }
    }

    private static final class UnbanIPCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String arg1 = args[0];
                PersistentUserData data = UserFileManager.get(arg1);
                boolean valid = isValidIP(arg1);
                if (data == null && !valid) {
                    sendMessage(sender, playerDataNotFound, arg1);
                    return false;
                }
                String ip = valid ? arg1 : data.getSavedIP().getHostAddress();
/*                if (!Bukkit.getBanList(BanList.Type.IP).isBanned(ip)) {
                    sendMessage(sender, ipNotBanned, ip);
                    return false;
                }*/
                unban(ip, true);
                sendMessage(sender, unbannedIp, ip);
                return true;
            } else {
                sendMessage(sender, formatUnbanip);
            }
            return false;
        }
    }

    //private static final boolean authCmdAsyncInvoked = PacketBlocker.serverboundNameVersion;

    /*public static void onSyncCaptchaCommand(UnverifiedUser user, Player sender, String captcha) {
        if (user.isCaptchaCorrect(captcha)) {
            user.completeCaptcha();
            sendMessage(sender, captchaComplete);
            return;
        }
        if (kickOnIncorrectCaptcha) {
            MethodProvider.kickAsync(user, incorrectCaptchaKickPacket);
            //sender.kickPlayer(incorrectCaptcha);
            return;
        }
        sendMessage(sender, incorrectCaptcha);
    }*/

    private static final ByteBuf incorrectCaptchaKickPacket = OutDisconnectPacketConstructor.constructConstAtPlayPhase(incorrectCaptcha);
    public static final ByteBuf
            incorrectCaptchaMessagePacket = OutMessagePacketConstructor.constructConst(incorrectCaptcha),
            captchaCompleteMessagePacket = OutMessagePacketConstructor.constructConst(requirePasswordRepeatInRegister ? Messages.getWithPrefix("captcha-complete-register-password-repeat") : Messages.getWithPrefix("captcha-complete"));

    public static void onCaptchaCompletionAttempt(UnverifiedUser user, String captcha) {
        //Main.logError("INPUT: '" + captcha + "'");
        if (user.isCaptchaCorrect(captcha)) {
            user.writeConstSilently(captchaCompleteMessagePacket);
            user.completeCaptcha();
            return;
        }
        if (++user.captchaAttempts == maxCaptchaAttempts) MethodProvider.kickAsync(user, incorrectCaptchaKickPacket);
        else user.writeAndFlushConstSilently(incorrectCaptchaMessagePacket);
    }

/*    private static final class CaptchaVerifyCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            sender.sendMessage(commandUnreachable);
            return false;
        }
    }*/

    public static void onPasswordChangeCommand(VerifiedUser user, String[] args) {
        Player sender = user.getPlayer();
        if (args.length != 1) {
            sendMessage(sender, formatChangepassword);
            return;
        }
        String arg1 = args[0];
        String reason = getInvalidityReason(arg1, false);
        if (reason == null) {
            user.changePassword(arg1);
            sendMessage(sender, passwordChanged);
            return;
        }
        sender.sendRawMessage(reason);
    }

    private static final class PasswordChangeCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            /**
             * The {@link VerifiedPacketProcessor}
             * should handle the processing of this command.
             * Only the console could've made the code here executed
             **/
            sender.sendMessage(commandUnreachable);
            return false;
        }
    }

    public static final ByteBuf incorrectPasswordKickPacket = OutDisconnectPacketConstructor.constructConstAtPlayPhase(Messages.getWithPrefix("incorrect-password"));
    public static final ByteBuf
            incorrectPasswordMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("incorrect-password")),
            captchaReminderMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("captcha-reminder-command")),
            registerReminderMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("register-reminder-command"));

/*    public static void onSyncLoginCommand(UnverifiedUser user, String password) {
        if (!user.hasCompletedCaptcha()) {
            user.writeAndFlushDuplicateSilently(captchaReminderMessagePacket);
            return;
        }
        if (!user.isRegistered()) {
            user.writeAndFlushDuplicateSilently(registerReminderMessagePacket);
            return;
        }
        if (user.isPasswordCorrect(password)) {
            user.logIn();
            return;
        }
        if (++user.loginAttempts == maxLoginAttempts) MethodProvider.kickAsync(user, incorrectPasswordKickPacket);
        else user.writeAndFlushDuplicateSilently(incorrectPasswordMessagePacket);
    }*/

    public static boolean onAsyncLoginCommand(UnverifiedUser user, String password) {
        if (!user.hasCompletedCaptcha()) {
            user.writeAndFlushConstSilently(captchaReminderMessagePacket);
            return false;
        }
        if (!user.isRegistered()) {
            user.writeAndFlushConstSilently(registerReminderMessagePacket);
            return false;
        }
        if (user.isPasswordCorrect(password)) {
            user.tryLogIn();
            return true;
        }
        if (++user.loginAttempts == maxLoginAttempts) MethodProvider.kickAsync(user, incorrectPasswordKickPacket);
        else user.writeAndFlushConstSilently(incorrectPasswordMessagePacket);
        return false;
    }

/*    private static final class LoginCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            sender.sendMessage(commandUnreachable);
            return false;
        }
    }*/

    public static final ByteBuf
            alreadyRegisteredMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("already-registered")),
            passwordRegisterMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("password-register")),
            registerPasswordsDoNotMatchMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("commands-register-passwords-do-not-match")),
            registerPasswordsMoreThanTwoMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("commands-register-passwords-more-than-two"));

/*    public static void onSyncRegisterCommand(UnverifiedUser user, String password) {
        if (!user.hasCompletedCaptcha()) {
            user.writeAndFlushDuplicateSilently(captchaReminderMessagePacket);
            return;
        }
        if (user == null || user.isRegistered()) {
            user.writeAndFlushDuplicateSilently(alreadyRegisteredMessagePacket);
            return;
        }
        String reason = getInvalidityReason(password, false);
        if (reason == null) {
            user.registerSync(password);
            user.writeAndFlushDuplicateSilently(passwordRegisterMessagePacket);
            return;
        }
        user.writeAndFlushDuplicateSilently(OutMessagePacketConstructor.constructConst(reason));
    }*/

    public static void onAsyncRegisterCommand(UnverifiedUser user, String args) {
        if (!user.hasCompletedCaptcha()) {
            user.writeAndFlushConstSilently(captchaReminderMessagePacket);
            return;
        }
        if (user.isRegistered()) {
            user.writeAndFlushConstSilently(alreadyRegisteredMessagePacket);
            return;
        }

        String password;

        if (requirePasswordRepeatInRegister) {
            String[] a = AlixUtils.split(args, ' ');

            switch (a.length) {
                case 1:
                    tryRegisterIfValid(user, a[0]);//tolerate single password input, even if repeat is explicitly enabled in config
                    return;
                case 2:
                    password = a[0];
                    if (!password.equals(a[1])) {
                        user.writeAndFlushConstSilently(registerPasswordsDoNotMatchMessagePacket);
                        return;
                    }
                    break;
                default:
                    user.writeAndFlushConstSilently(registerPasswordsMoreThanTwoMessagePacket);
                    return;
            }

        } else password = args;

        tryRegisterIfValid(user, password);
    }

    //returns true if valid
    public static boolean tryRegisterIfValid(UnverifiedUser user, String password) {
        String reason = getInvalidityReason(password, false);
        if (reason == null) {
            user.registerAsync(password);
            user.writeAndFlushConstSilently(passwordRegisterMessagePacket);
            return true;
        }
        user.sendDynamicMessageSilently(reason);
        return false;
    }

    /*    private static final class RegisterCommand implements CommandExecutor {

            @Override
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
                sender.sendMessage(commandUnreachable);
                return false;
            }
        }*/

    private static final class PremiumCommand implements CommandExecutor {

        private static final ByteBuf
                alreadyPremiumMessage = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("premium-command-already-premium")),
                premiumDataMessage = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("premium-command-premium")),
                nonPremiumDataMessage = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("premium-command-non-premium")),
                unknownDataMessage = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("premium-command-unknown"));

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            Player player = (Player) sender;
            var user = UserManager.get(player.getUniqueId());
            var channel = user.getChannel();
            var uuid = AlixChannelHandler.getLoginAssignedUUID(channel);
            boolean canBePremium = uuid == null || uuid.version() == 4;

            if (!canBePremium) {
                user.writeAndFlushSilently(nonPremiumDataMessage);
                return false;
            }

            String name = player.getName();
            var data = UserFileManager.get(name);

            if (data.getPremiumData().getStatus().isPremium()) {
                user.writeAndFlushSilently(alreadyPremiumMessage);
                return false;
            }

            AlixScheduler.asyncBlocking(() -> {
                PremiumData premiumData = PremiumDataCache.getOrUnknown(name);
                if (premiumData.getStatus().isUnknown()) {
                    premiumData = PremiumUtils.requestPremiumData(name);
                    if (premiumData.getStatus().isKnown()) PremiumDataCache.add(name, premiumData);
                }

                switch (premiumData.getStatus()) {
                    case PREMIUM: {
                        user.writeAndFlushSilently(premiumDataMessage);
                        data.setPremiumData(premiumData);
                        return;
                    }
                    case NON_PREMIUM: {
                        user.writeAndFlushSilently(nonPremiumDataMessage);
                        return;
                    }
                    case UNKNOWN: {
                        user.writeAndFlushSilently(unknownDataMessage);
                    }
                }
            });
            return true;
        }
    }

    private static final class AccountSettingsCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            AccountGUI.add((Player) sender);
            return true;
        }
    }

    private static final class NickNameCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            if (label.equalsIgnoreCase("nicknameplayer")) {
                if (l == 0) {
                    sendMessage(sender, formatNicknamePlayer);
                    return false;
                }
                String arg1 = args[0];
                Player p = Bukkit.getPlayerExact(arg1);
                if (p == null) {
                    sendMessage(sender, playerNotFound, arg1);
                    return false;
                }
                if (l == 1) {
                    setName(p, null);
                    sendMessage(sender, nicknamePlayerReset, p.getName());
                    return true;
                }
                String nickname = translateColors(mergeWithSpacesAndSkip(args, 1));
                setName(p, nickname);
                sendMessage(sender, nicknamePlayerSet, p.getName(), nickname);
                return true;
            }
            if (l == 0) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                setName(p, null);
                sendMessage(sender, nicknameReset);
                return true;
            }
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            String nickname = translateColors(setAsOneAndAddAfter(args, " "));
            setName(p, nickname);
            sendMessage(sender, nicknameChangeSelf, nickname);
            return true;
//sendMessage(sender, "Format: /nickname or /nickname <name>, or /nicknameplayer <player> <name>");
            //return false;
        }
    }

    private static final class OnlinePlayersListCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            Collection<? extends Player> onlines = Bukkit.getOnlinePlayers();
            int l = onlines.size();
            if (l == 0) {
                sendMessage(sender, nonePlayers);
                return false;
            }
            sendMessage(sender, getListOfAllOnlinePlayers(onlines) + "\n" + onlinePlayerList, l);
            return true;
        }
    }

    private static final class GamemodeSwitchCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    switch (label) {
                        case "gm0":
                        case "gms":
                            p.setGameMode(GameMode.SURVIVAL);
                            sendMessage(sender, gamemodeSurvivalSelf);
                            break;
                        case "gm1":
                        case "gmc":
                            p.setGameMode(GameMode.CREATIVE);
                            sendMessage(sender, gamemodeCreativeSelf);
                            break;
                        case "gm2":
                        case "gma":
                            p.setGameMode(GameMode.ADVENTURE);
                            sendMessage(sender, gamemodeAdventureSelf);
                            break;
                        case "gm3":
                        case "gmsp":
                            p.setGameMode(GameMode.SPECTATOR);
                            sendMessage(sender, gamemodeSpectatorSelf);
                            break;
                        default:
                            sendMessage(sender, invalidCommand);
                            break;
                    }
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p2 = Bukkit.getPlayerExact(arg1);
                    if (p2 != null) {
                        String name = p2.getName();
                        switch (label) {
                            case "gm0":
                            case "gms":
                                p2.setGameMode(GameMode.SURVIVAL);
                                sendMessage(sender, gamemodeSurvivalPlayer, name);
                                break;
                            case "gm1":
                            case "gmc":
                                p2.setGameMode(GameMode.CREATIVE);
                                sendMessage(sender, gamemodeCreativePlayer, name);
                                break;
                            case "gm2":
                            case "gma":
                                p2.setGameMode(GameMode.ADVENTURE);
                                sendMessage(sender, gamemodeAdventurePlayer, name);
                                break;
                            case "gm3":
                            case "gmsp":
                                p2.setGameMode(GameMode.SPECTATOR);
                                sendMessage(sender, gamemodeSpectatorPlayer, name);
                                break;
                            default:
                                sendMessage(sender, invalidCommand);
                                break;
                        }
                    } else if (!isConsoleButPlayerRequired(sender)) {
                        Player p = (Player) sender;
                        if (isNumber(arg1)) {
                            int gamemode = Integer.parseInt(arg1);
                            switch (gamemode) {
                                case 0:
                                    p.setGameMode(GameMode.SURVIVAL);
                                    sendMessage(sender, gamemodeSurvivalSelf);
                                    break;
                                case 1:
                                    p.setGameMode(GameMode.CREATIVE);
                                    sendMessage(sender, gamemodeCreativeSelf);
                                    break;
                                case 2:
                                    p.setGameMode(GameMode.ADVENTURE);
                                    sendMessage(sender, gamemodeAdventureSelf);
                                    break;
                                default:
                                    p.setGameMode(GameMode.SPECTATOR);
                                    sendMessage(sender, gamemodeSpectatorSelf);
                                    break;
                            }
                        } else {
                            GameMode gamemode = getGameModeType(arg1);
                            p.setGameMode(gamemode);
                            sendMessage(sender, gamemodeSet, gamemode.toString().toLowerCase());
                        }
                    }
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String arg2 = args[1];
                    Player p = Bukkit.getPlayerExact(arg2);
                    if (p == null) {
                        sendMessage(sender, playerNotFound, arg2);
                        return false;
                    }
                    String name = p.getName();
                    if (isNumber(arg1)) {
                        int gamemode = Integer.parseInt(arg1);
                        switch (gamemode) {
                            case 0:
                                p.setGameMode(GameMode.SURVIVAL);
                                sendMessage(sender, gamemodeSurvivalPlayer, name);
                                break;
                            case 1:
                                p.setGameMode(GameMode.CREATIVE);
                                sendMessage(sender, gamemodeCreativePlayer, name);
                                break;
                            case 2:
                                p.setGameMode(GameMode.ADVENTURE);
                                sendMessage(sender, gamemodeAdventurePlayer, name);
                                break;
                            default:
                                p.setGameMode(GameMode.SPECTATOR);
                                sendMessage(sender, gamemodeSpectatorPlayer, name);
                                break;
                        }
                    } else {
                        GameMode gamemode = getGameModeType(arg1);
                        p.setGameMode(gamemode);
                        sendMessage(sender, gamemodePlayerSetPlayer, name, gamemode.toString().toLowerCase());
                    }
                    return true;
                }
                default:
                    sendMessage(sender, formatGamemode);
                    break;
            }
            return false;
        }
    }

    private static final class InventoryViewCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length != 0) {
                if (isConsoleButPlayerRequired(sender)) return false;
                String arg1 = args[0];
                Player p = (Player) sender;
                Player p2 = Bukkit.getPlayerExact(arg1);
                if (p2 == null) {
                    sendMessage(sender, playerNotFound, arg1);
                    return false;
                }
                p.openInventory(p2.getInventory());
                return true;
            }
            sendMessage(sender, formatInvsee);
            return false;
        }
    }

    private static final class EnderChestCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    p.openInventory(p.getEnderChest());
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = (Player) sender;
                    Player p2 = Bukkit.getPlayerExact(arg1);
                    if (p2 == null) {
                        sendMessage(sender, playerNotFound, arg1);
                        return false;
                    }
                    p.openInventory(p2.getEnderChest());
                    return true;
                }
                default:
                    sendMessage(sender, formatEnderchest);
                    break;
            }
            return false;
        }
    }

    private static final class FeedCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    p.setFoodLevel(20);
                    sendMessage(sender, feedSelf);
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, playerNotFound, arg1);
                        return false;
                    }
                    p.setFoodLevel(20);
                    sendMessage(sender, playerFeed, p.getName());
                    return true;
                }
                default:
                    sendMessage(sender, formatFeed);
                    break;
            }
            return false;
        }
    }

    /*private static final class VanishCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    User u = getUser(p);
                    if (u.isVanished()) {
                        u.setVanished(false);
                        sendMessage(sender, "Vanish has been &cdisabled!");
                    } else {
                        u.setVanished(true);
                        sendMessage(sender, "Vanish has been &aenabled!");
                    }
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, playerNotFound, arg1);
                        return false;
                    }
                    String name = p.getName();
                    User u = getUser(p);
                    if (u.isVanished()) {
                        u.setVanished(false);
                        sendMessage(sender, "Vanish for player " + name + " has been &cdisabled!");
                    } else {
                        u.setVanished(true);
                        sendMessage(sender, "Vanish for player " + name + " has been &aenabled!");
                    }
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /vanish or /vanish <player>");
                    break;
            }
            return false;
        }
    }*/

    private static final class HealCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    Collection<PotionEffect> potions = p.getActivePotionEffects();
                    for (PotionEffect pe : potions) p.removePotionEffect(pe.getType());
                    p.setHealth(p.getMaxHealth());
                    p.setFoodLevel(20);
                    p.setFireTicks(0);
                    sendMessage(sender, healSelf);
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, playerNotFound, arg1);
                        return false;
                    }
                    String name = p.getName();
                    Collection<PotionEffect> potions = p.getActivePotionEffects();
                    for (PotionEffect pe : potions) p.removePotionEffect(pe.getType());
                    p.setHealth(p.getMaxHealth());
                    p.setFoodLevel(20);
                    p.setFireTicks(0);
                    sendMessage(sender, playerHealed, name);
                    return true;
                }
                default:
                    sendMessage(sender, formatHeal);
                    break;
            }
            return false;
        }
    }

    private static final class SpeedCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            switch (l) {
                case 1: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    String arg1 = args[0];
                    if (!isNumber(arg1)) {
                        sendMessage(sender, notANumber, arg1);
                        return false;
                    }
                    float n = Float.parseFloat(arg1);
                    if (p.isFlying()) {
                        p.setFlySpeed(clampOfOne(n * doubledDefaultFlySpeed));
                        sendMessage(sender, flyingSpeedSelf, setAsClearNumber(n));
                    } else {
                        p.setWalkSpeed(clampOfOne(n * doubledDefaultWalkSpeed));
                        sendMessage(sender, walkingSpeedSelf, setAsClearNumber(n));
                    }
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String arg2 = args[1];
                    Player p = Bukkit.getPlayerExact(arg2);
                    if (p == null) {
                        sendMessage(sender, playerNotFound, arg2);
                        return false;
                    }
                    String name = p.getName();

                    if (!isNumber(arg1)) {
                        sendMessage(sender, notANumber, arg1);
                        return false;
                    }
                    float n = Float.parseFloat(arg1);
                    if (p.isFlying()) {
                        p.setFlySpeed(clampOfOne(n * doubledDefaultFlySpeed));
                        sendMessage(sender, flyingSpeedPlayer, name, setAsClearNumber(n));
                    } else {
                        p.setWalkSpeed(clampOfOne(n * doubledDefaultWalkSpeed));
                        sendMessage(sender, walkingSpeedPlayer, name, setAsClearNumber(n));
                    }
                    return true;
                }
                case 3: {
                    String arg1 = args[0];
                    String arg2 = args[1];
                    String arg3 = args[2];
                    Player p = Bukkit.getPlayerExact(arg2);
                    if (p == null) {
                        sendMessage(sender, playerNotFound, arg2);
                        return false;
                    }
                    String name = p.getName();

                    if (!isNumber(arg1)) {
                        sendMessage(sender, notANumber, arg1);
                        return false;
                    }
                    float n = Float.parseFloat(arg1);
                    if (arg3.equalsIgnoreCase("fly")) {
                        p.setFlySpeed(clampOfOne(n * doubledDefaultFlySpeed));
                        sendMessage(sender, flyingSpeedPlayer, name, setAsClearNumber(n));
                    } else if (arg3.equalsIgnoreCase("walk")) {
                        p.setWalkSpeed(clampOfOne(n * doubledDefaultWalkSpeed));
                        sendMessage(sender, walkingSpeedPlayer, name, setAsClearNumber(n));
                    } else
                        sendMessage(sender, speedInvalidArg, arg3);
                    return true;
                }
                default:
                    sendMessage(sender, formatSpeed);
                    break;
            }
            return false;
        }
    }

    private static final class ItemRenameCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 0) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                ItemStack i = p.getInventory().getItemInMainHand();
                ItemMeta im = i.getItemMeta();
                if (im == null) {
                    sendMessage(sender, itemAbsentDuringRenaming);
                    return false;
                }
                im.setDisplayName(null);
                i.setItemMeta(im);
                sendMessage(sender, itemNameReset);
                return true;
            }
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            ItemStack i = p.getInventory().getItemInMainHand();
            ItemMeta im = i.getItemMeta();
            if (im == null) {
                sendMessage(sender, itemAbsentDuringRenaming);
                return false;
            }
            im.setDisplayName(translateColors(setAsOneAndAddAfter(args, " ")));
            i.setItemMeta(im);
            sendMessage(sender, renamedItem);
            return false;
        }
    }

    private static final class FlyCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    if (p.isFlying()) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        sendMessage(sender, flyingEnabled);
                    } else {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                        sendMessage(sender, flyingDisabled);
                    }
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, playerNotFound, arg1);
                        return false;
                    }
                    String name = p.getName();
                    if (p.isFlying()) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        sendMessage(sender, flyingPlayerDisabled, name);
                    } else {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                        sendMessage(sender, flyingPlayerEnabled, name);
                    }
                    return true;
                }
                default:
                    sendMessage(sender, formatFly);
                    break;
            }
            return false;
        }
    }


    /*private static final class OdpowiedzKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length != 0) {
                String replier = sender.getName();
                String originalSender = PersonalMessageManager.get(replier);

                if (originalSender == null) {
                    sendMessage(sender, "&cNie posiadasz konwersacji na którą mógłbyś odpowiedzieć!");
                    return false;
                }
                Player p = Bukkit.getPlayerExact(originalSender);
                if (p == null) {
                    sendMessage(sender, "&cGracz " + originalSender + " nie jest już aktywny!");
                    return false;
                }
                String message = mergeWithSpaces(args);

                sender.sendMessage(translateColors("&6[&cJa &6-> &c" + originalSender + "&6]: &7") + message);
                p.sendMessage(translateColors("&6[&c" + replier + " &6-> &cJa&6]: &7") + message);

                PersonalMessageManager.add(originalSender, replier); //For /msg shortcut
                return true;
            }
            sendMessage(sender, "Format: /r <wiadomość>");
            return false;
        }
    }

    private static final class OsobistaWiadomoscKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length > 1) {
                String arg1 = args[0];
                Player p = Bukkit.getPlayerExact(arg1);
                if (p == null) {
                    sendMessage(sender, "&cNie znaleziono gracza o nazwie " + arg1 + "!");
                    return false;
                }

                String pName = p.getName();
                String sName = sender.getName();

                String message = mergeWithSpacesAndSkip(args, 1);

                sender.sendMessage(translateColors("&6[&cJa &6-> &c" + pName + "&6]: &7") + message);
                p.sendMessage(translateColors("&6[&c" + sName + " &6-> &cJa&6]: &7") + message);

                PersonalMessageManager.add(sName, pName);
                return true;
            }
            sendMessage(sender, "Format: /msg <gracz> <wiadomość>");
            return false;
        }
    }

    private static final class SudoKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length > 1) {
                String arg1 = args[0];
                Player p = Bukkit.getPlayerExact(arg1);
                if (p == null) {
                    sendMessage(sender, "&cNie znaleziono gracza o nazwie " + arg1 + "!");
                    return false;
                }
                String arg2 = args[1];
                if (startsWith(arg2, "c:", "czat:", "t:", "type:")) {
                    String toChat = removeUntilCharFound(mergeWithSpaces(args), ':'); //mergeWithSpacesAndSkip(args, 2);
                    sendMessage(sender, "Wymuszono na graczu " + arg1 + " wysłanie wiadomości:");
                    p.chat(toChat);
                    return true;
                }
                String toCommandSend = unslashify(mergeWithSpacesAndSkip(args, 1));
                if (!commandExists(JavaUtils.split(toCommandSend, ' ')[0])) {
                    sendMessage(sender, "&cDana komenda nie istnieje!");
                    return false;
                }

                if (p.performCommand(toCommandSend)) {//success
                    sendMessage(sender, "Wymuszono na graczu " + arg1 + " wysłanie komendy: " + toCommandSend);
                    return true;
                }
                sendMessage(sender, "&cNieprawidłowy format wymuszanej komendy!");
                return false;
            }
            sendMessage(sender, "Format: /sudo <gracz> <komenda> or /sudo <gracz> czat: <wiadomość>");
            return false;
        }
    }

    private static final class TeleportacjaDoSpawnaKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 0) {
                Player p = (Player) sender;
                SpawnFileManager.teleport(p);
                sendMessage(sender, "&6Teleportowano do spawna!");
                return true;
            }
            sendMessage(sender, "Format: /spawn");
            return false;
        }
    }

    private static final class UstawienieSpawnaKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 0) {
                Player p = (Player) sender;
                SpawnFileManager.set(p.getLocation());
                sendMessage(sender, "&6Pomyślnie ustawiono spawna!");
                return true;
            }
            sendMessage(sender, "Format: /setspawn");
            return false;
        }
    }

    private static final class OdciszenieKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String arg1 = args[0];*//*
                OfflinePlayer offlinePlayer = getOfflinePlayer(arg1);
                if (offlinePlayer == null) {
                    sendMessage(sender, "&cERROR: Gracz " + arg1 + " nigdy nie dołączył do tego serwer!");
                    return false;
                }
                String name = offlinePlayer.getName();*//*
                PersistentUserData data = UserFileManager.get(arg1);
                if (data == null) {
                    sendMessage(sender, "&cNie znaleziono żadnych informacji o graczu " + arg1 + " w bazie danych AlixSystem!");
                    return false;
                }
                sendMessage(sender, "Odciszono gracza " + arg1 + ".");
                data.setMutedUntil(0);
                return true;
            }
            sendMessage(sender, "Format: /unmute <gracz>");
            return false;
        }
    }

    private static final class WyciszenieKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            switch (args.length) {
                case 1: {
                    String arg1 = args[0];*//*
                    OfflinePlayer offlinePlayer = getOfflinePlayer(arg1);
                    if (offlinePlayer == null) {
                        sendMessage(sender, "&cPlayer " + arg1 + " has never joined this server before!");
                        return false;
                    }*//*
                    PersistentUserData data = UserFileManager.get(arg1);
                    if (data == null) {
                        sendMessage(sender, "&cNie znaleziono żadnych informacji o graczu " + arg1 + " w Bazie Danych Użytkowników AlixSystem!");
                        return false;
                    }*//*
                    if (offlinePlayer.isOp()) {
                        sendMessage(sender, "&cGracz " + arg1 + " jest operatorem, więc nie może być wyciszony!");
                        return false;
                    }*//*
                    sendMessage(sender, "Wyciszono " + arg1 + " na zawsze!");
                    data.setMutedUntil(0x9b627f782d8L); //9b627f782d8 -> 10677959230168L as for 1677959230168L, which is the time this code was written in,
                    //but in hex to look fancy :>
                }
                return true;
                case 2: {
                    String arg1 = args[0];
                    PersistentUserData data = UserFileManager.get(arg1);
                    if (data == null) {
                        sendMessage(sender, "&cNie znaleziono żadnych informacji o graczu " + arg1 + " w Bazie Danych Użytkowników AlixSystem!");
                        return false;
                    }
                    long time = getProcessedTime(args[1]);
                    data.setMutedUntil(time);
                    sendMessage(sender, "Wyciszono " + arg1 + " do " + getFormattedDate(new Date(time)));
                }
                return true;
            }
            sendMessage(sender, "Format: /mute <gracz> lub /mute <gracz> <czas>");
            return false;
        }
    }

    private static final class UstawianieOffPrzyjomowaniaZapytanTeleportacji implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            UserManager.getVerifiedUser((Player) sender).setCanReceiveTeleportRequests(false);
            sendMessage(sender, "Przestajesz przyjmować zapytania odnośnie teleportacji.");
            return true;
        }
    }

    private static final class UstawianieOnPrzyjomowaniaZapytanTeleportacji implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            UserManager.getVerifiedUser((Player) sender).setCanReceiveTeleportRequests(true);
            sendMessage(sender, "Rozpoczynasz przyjmowanie zapytań odnośnie teleportacji. ");
            return true;
        }
    }

    private static final class CofniecieTeleportacjiKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 0) {
                Player p = (Player) sender;
                TpaRequest request = TpaManager.getRequest(p.getName());
                if (request == null) {
                    sendMessage(p, "&cNie posiadasz żadnego zapytania teleportacji.");
                    return false;
                }
                request.remove();
                sendMessage(p, "Cofnąłeś swoje zapytanie odnośnie teleportacji");
                return true;
            }
            sendMessage(sender, "Format: /tpacancel");
            return false;
        }
    }

    private static final class OdrzucenieTeleportacjiKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length != 0) {
                Player p = (Player) sender;
                String arg1 = args[0];
                Player p2 = Bukkit.getPlayerExact(arg1);
                if (p2 == null) {
                    sendMessage(sender, "&cGracz " + arg1 + " nie jest obecnie online");
                    return false;
                }
                String name = p2.getName();
                TpaRequest request = TpaManager.getRequest(name);
                if (request == null) {
                    sendMessage(p, "&cNie posiadasz zapytania teleportacji od " + name + "!");
                    return false;
                }
                request.remove();
                sendMessage(p, "Odrzuciłeś zapytanie teleportacji od gracza " + name + ".");
                sendMessage(p2, "Twoje zapytanie odnośnie teleportacji zostało odrzucone.");
                return true;
            }
            sendMessage(sender, "Format: /tpadeny <gracz>");
            return false;
        }
    }

    private static final class AkceptacjaTeleportacjiKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length != 0) {
                Player p = (Player) sender;
                String arg1 = args[0];
                Player p2 = Bukkit.getPlayerExact(arg1);
                if (p2 == null) {
                    sendMessage(sender, "&cGracz " + arg1 + " nie jest obecnie online");
                    return false;
                }
                String name = p2.getName();
                TpaRequest request = TpaManager.getRequest(name);
                if (request == null) {
                    sendMessage(p, "&cNie posiadasz zapytania teleportacji od " + name + "!");
                    return false;
                }
                request.accept();
                sendMessage(p, "Zaakceptowałeś zapytanie odnośnie teleportacji od gracza " + name + ".");
                sendMessage(p2, "Twoje zapytanie odnośnie teleportacja została zaakceptowane.");
                return true;
            }
            sendMessage(sender, "Format: /tpaccept <gracz>");
            return false;
        }
    }

    private static final class ZapytanieTeleportacjiKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length != 0) {
                Player p = (Player) sender;
                String arg1 = args[0];
                Player p2 = Bukkit.getPlayerExact(arg1);
                if (p2 == null) {
                    sendMessage(sender, "&cGracz " + arg1 + " nie jest obecnie online.");
                    return false;
                }
                if (TpaManager.hasRequestsOff(p2)) {
                    sendMessage(sender, "&cGracz " + arg1 + " posiada wyłączone przyjmowanie zapytań odnośnie telportacji");
                    return false;
                }
                String name = p.getName();
                TpaRequest request = TpaManager.getRequest(name);
                if (request != null) {
                    String name2 = request.getSentToName();
                    sendMessage(p, "&cWysłałeś już zapytanie odnośnie teleportacji do " + name2 + "! Jeżeli checesz " +
                            "je cofnąć, to wpisz /tpacancel.");
                    return false;
                }
                TpaManager.addRequest(name, p, p2);
                String name2 = p2.getName();
                sendMessage(p, "Wysłałeś zapytanie odnośnie teleportacji do " + name2 + ". " +
                        "Jeżeli checesz je cofnąć, to wpisz &c/tpacancel&7. W przeciwnym razie " +
                        "automatycznie wygaśnie w ciągu &c120 sekund&7.");
                sendMessage(p2, "Otrzymałeś zapytanie odnośnie teleportacji od " + name + ". " +
                        "Jeżeli chcesz je zaakceptować/odrzucić, to wpisz &c/tpaccept " + name +
                        " &7lub &c/tpadeny " + name + "&7.  W przeciwnym razie automatycznie wygaśnie w ciągu &c120 sekund&7.");
                return true;
            }
            sendMessage(sender, "Format: /tpa <gracz>");
            return false;
        }
    }

    private static final class ListaDomowKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            if (args.length == 0) {
                NamedLocation[] homes = getVerifiedUser(p).getHomes().asArray();
                if (homes.length == 0) {
                    sendMessage(sender, "Nie ustawiono jeszcze żadnych domów!");
                    return false;
                }
                sendMessage(sender, "Lista instniejących domów: ");
                sendMessage(sender, "");
                for (NamedLocation h : homes) sendMessage(sender, h.toUserReadable());
                sendMessage(sender, "");
                return true;
            }
            sendMessage(sender, "Format: /homes");
            return false;
        }
    }

    private static final class TeleportacjaDoDomuKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            User u = getVerifiedUser(p);
            switch (l) {
                case 0: {
                    NamedLocation home = u.getHomes().getByName("default");
                    if (home != null) {
                        home.teleport(p);
                        sendMessage(sender, "&6Teleportowano!");
                        return true;
                    }
                    sendMessage(sender, "&cTwój dom nie został nigdy ustawiony!");
                    return false;
                }
                case 1: {
                    String name = args[0];
                    NamedLocation home = u.getHomes().getByName(name);
                    if (home != null) {
                        home.teleport(p);
                        sendMessage(sender, "&6Teleportowano do domu o nazwie " + name + "!");
                        return true;
                    }
                    sendMessage(sender, "&cDom o nazwa " + name + " nie został nigdy ustawiony!");
                    return false;
                }
                default:
                    sendMessage(sender, "Format: /home lub /home <nazwa>");
                    return false;
            }
        }
    }

    private static final class WarpUsunKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String name = args[0];
                if (WarpFileManager.remove(name) != null) {
                    sendMessage(sender, "&6Usunięto warp " + name + "!");
                    return true;
                }
                sendMessage(sender, "&cWarp " + name + " nie istnieje!");
                return false;
            }
            sendMessage(sender, "Format: /usunwarp <nazwa>");
            return false;
        }
    }

    private static final class WarpStworzKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 1) {
                Player p = (Player) sender;
                Location l = p.getLocation();
                String name = args[0];
                NamedLocation NamedLocation = WarpFileManager.get(name);
                if (NamedLocation != null) {
                    WarpFileManager.replace(new NamedLocation(l, NamedLocation.getName()));
                    sendMessage(sender, "&6Poprawnie zmieniono lokalizację warpu " + name + "!");
                    return true;
                }
                WarpFileManager.add(new NamedLocation(l, name));
                sendMessage(sender, "&6Poprawnie stworzono warp " + name + "!");
                return true;
            }
            sendMessage(sender, "Format: /dodajwarp <name>");
            return false;
        }
    }

    private static final class WarpTeleportacjaKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (isConsoleButPlayerRequired(sender)) return false;
            if (args.length == 1) {
                Player p = (Player) sender;
                String name = args[0];
                NamedLocation warp = WarpFileManager.get(name);
                if (warp != null) {
                    warp.teleport(p);
                    sendMessage(sender, "&6Teleportowano do warpu " + name + "!");
                    return true;
                }
                sendMessage(sender, "&cWarp " + name + " nie istnieje!");
                return false;
            }
            sendMessage(sender, "Format: /warp <nazwa>");
            return false;
        }
    }

    private static final class UsuniecieDomuKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                User u = getVerifiedUser(p);
                String name = args[0];
                int i = u.getHomes().indexOf(name);
                if (i != -1) {
                    u.getHomes().removeHome(i);
                    sendMessage(sender, "&6Usunięto twój " + name + " dom.");
                    return true;
                }
                sendMessage(sender, "&cDom o nazwie " + name + " nie został ustawiony!");
                return false;
            }
            sendMessage(sender, "Format: /removehome <nazwa>");
            return false;
        }
    }

    private static final class UstawienieDomuKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            Location loc = p.getLocation();
            User u = getVerifiedUser(p);
            int current = u.getHomes().asArray().length;
            short max = u.getMaxHomes();
            switch (l) {
                case 0:
                    if (current < max) {
                        int i = u.getHomes().indexOf("default");
                        if (i != -1) {
                            u.getHomes().setHome(i, new NamedLocation(loc, "default"));
                            sendMessage(sender, "&6Twój stary, domyślny dom został poprawnie nadpisany i ustawiony na twoją lokalizację!");
                        } else {
                            u.getHomes().addHome(new NamedLocation(loc, "default"));
                            sendMessage(sender, "&6Pomyślnie ustawiono dom!");
                        }
                    } else
                        sendMessage(sender, "&cMaksymalna liczba domów została już przez ciebie osiągnięta! (" + max + ")");
                    break;
                case 1:
                    String name = args[0];
                    if (current < max) {
                        if (name.equals("default")) {
                            sendMessage(sender, "&cNieprawidłowa nazwa, proszę użyć innej!");
                            return false;
                        }
                        String reason = getInvalidityReason(name, true);
                        if (reason != null) {
                            sendMessage(sender, reason);
                            return false;
                        }
                        int i = u.getHomes().indexOf(name);
                        if (i != -1) {
                            u.getHomes().setHome(i, new NamedLocation(loc, name));
                            sendMessage(sender, "&6Twój stary dom nazwany " + name + " został pomyślnie nadpisany i ustawiony na twoją obecną lokalizację!");
                        } else {
                            u.getHomes().addHome(new NamedLocation(loc, name));
                            sendMessage(sender, "&6Pomyślnie ustawiono dom!");
                        }
                    } else
                        sendMessage(sender, "&cMaksymalna liczba domów została już przez ciebie osiągnięta! (" + max + ")");
                    break;
                default:
                    sendMessage(sender, "Format: /sethome lub /sethome <nazwa>");
                    break;
            }
            return false;
        }
    }

    private static final class UsuniecieOperatoraKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            boolean consoleSender = sender instanceof ConsoleCommandSender;
            int l = args.length;
            if (l == 1 && (!isOperatorCommandRestricted || consoleSender)) {
                String arg1 = args[0];
                OfflinePlayer p = getOfflinePlayer(arg1);
                if (p == null) {
                    sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy wcześniej nie wszedł na ten serwer!");
                    JavaHandler.handleOperatorUnsetPL(sender, arg1);
                    return false;
                }
                if (!p.isOp()) {
                    sendMessage(sender, "&eUWAGA: Gracz " + p.getName() + " nie był operatorem.");
                    return false;
                }
                p.setOp(false);
                broadcastToPermitted("&7Gracz " + p.getName() + " został zdeoppowany przez " + sender.getName());
                return true;
            } else if (l == 2 && isOperatorCommandRestricted) {
                String arg1 = args[0];
                String arg2 = args[1];
                OfflinePlayer p = getOfflinePlayer(arg1);
                boolean correctPassword = arg2.equals(operatorCommandPassword);
                if (p == null) {
                    if (!correctPassword) {
                        sendMessage(sender, "&cIncorrect password!");
                        return false;
                    }
                    JavaHandler.handleOperatorUnsetPL(sender, arg1);
                    return false;
                }
                if (!p.isOp()) {
                    sendMessage(sender, "&eUWAGA: Gracz " + p.getName() + " nie był operatorem.");
                    return false;
                }
                if (correctPassword || consoleSender) {
                    if (!p.isOp()) sendMessage(sender, "&cGracz " + p.getName() + " nie był operatorem.");
                    p.setOp(false);
                    broadcastToPermitted("&7Gracz " + p.getName() + " został zdeopowany przez " + sender.getName());
                } else sendMessage(sender, "&cNieprawidłowe hasło!");
                return true;
            } else
                sendMessage(sender, "Format: " + (isOperatorCommandRestricted ? "/deop <gracz> <hasło>" : "/deop <gracz>"));
            return false;
        }
    }

    private static final class UstawienieOperatoraKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            boolean consoleSender = sender instanceof ConsoleCommandSender;
            int l = args.length;
            if (l == 1 && (!isOperatorCommandRestricted || consoleSender)) {
                String arg1 = args[0];
                OfflinePlayer p = getOfflinePlayer(arg1);
                if (p == null) {
                    sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy wcześniej nie wszedł na ten serwer!");
                    JavaHandler.handleOperatorSetPL(sender, arg1);
                    return false;
                }
                if (p.isOp()) {
                    sendMessage(sender, "&cERROR: Gracz " + p.getName() + " już jest operatorem.");
                    return false;
                }
                p.setOp(true);
                broadcastToPermitted("&7Gracz " + p.getName() + " został operatorem dzięki " + sender.getName());
                return true;
            } else if (l == 2 && isOperatorCommandRestricted) {
                String arg1 = args[0];
                String arg2 = args[1];
                OfflinePlayer p = getOfflinePlayer(arg1);
                boolean correctPassword = arg2.equals(operatorCommandPassword);
                if (p == null) {
                    if (!correctPassword) {
                        sendMessage(sender, "&cNieprawidłowe hasło!");
                        return false;
                    }
                    sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy wcześniej nie wszedł na ten serwer!");
                    JavaHandler.handleOperatorSetPL(sender, arg1);
                    return false;
                }
                if (correctPassword || consoleSender) {
                    if (p.isOp()) {
                        sendMessage(sender, "&cERROR: Gracz " + p.getName() + " już jest operatorem.");
                        return false;
                    }
                    p.setOp(true);
                    broadcastToPermitted("&7Gracz " + p.getName() + " został operatorem dzięki " + sender.getName());
                } else sendMessage(sender, "&cNieprawidłowe hasło!");
                return true;
            } else
                sendMessage(sender, "Format: " + (isOperatorCommandRestricted ? "/op <gracz> <hasło>" : "/op <gracz>"));
            if (isOperatorCommandRestricted)
                sendMessage(sender, "&cW razie niepewności co do danego hasło, proszę sprawdż plik config.yml tego pluginu " +
                        "lub używać tej komendy jedynie przez konsolę.");
            return false;
        }
    }

    private static final class TymczasowyBanIPKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 1: {
                    String arg1 = args[0];
                    boolean valid = isValidIP(arg1);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null && !valid) {
                        sendMessage(sender, "&cERROR: Gracz " + arg1 + " nigdy nie dołączył do tego serwer!");
                        return false;
                    }

                    PersistentUserData data = UserFileManager.get(p.getName());
                    String address = valid ? arg1 : data == null ? "0" : data.getSavedIP();
                    if (address.equals("0")) {
                        sendMessage(sender, "&cNie znaleziono żadnych informacji o ip dla " + p.getName() +
                                ", przez co ban na ip się nie udał.");
                        return false;
                    }
                    ban(address, "Zostałeś zbanowany!", null, true, sender.getName());
                    sendMessage(sender, "Zbanowano ip &c" + address + " &7na zawsze!");
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String reason = translateColors(args[1]);
                    boolean valid = isValidIP(arg1);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null && !valid) {
                        sendMessage(sender, "&cERROR: Gracz " + arg1 + " nigdy nie dołączył do tego serwer!");
                        return false;
                    }

                    PersistentUserData data = UserFileManager.get(p.getName());
                    String address = valid ? arg1 : data == null ? "0" : data.getSavedIP();
                    if (address.equals("0")) {
                        sendMessage(sender, "&cNie znaleziono żadnych informacji o ip dla " + p.getName() +
                                ", przez co ban na ip się nie udał.");
                        return false;
                    }
                    ban(address, reason, null, true, sender.getName());
                    sendMessage(sender, "Zbanowano ip &c" + address + " &7za &c" + reason + "&7!");
                    return true;
                }
                case 3: {
                    String arg1 = args[0];
                    String reason = translateColors(args[1]);
                    String arg3 = args[2];
                    boolean valid = isValidIP(arg1);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    if (p == null && !valid) {
                        sendMessage(sender, "&cERROR: Gracz " + arg1 + " nigdy nie dołączył do tego serwer!");
                        return false;
                    }
                    PersistentUserData data = UserFileManager.get(p.getName());
                    String address = valid ? arg1 : data == null ? "0" : data.getSavedIP();

                    if (address.equals("0")) {
                        sendMessage(sender, "&cNie znaleziono żadnych informacji o ip dla " + p.getName() +
                                ", przez co ban na ip się nie udał.");
                        return false;
                    }
                    Date date = getProcessedDate(arg3);
                    ban(address, reason, date, true, sender.getName());
                    broadcastToPermitted("Zbanowano ip &c" + address + " &7do &c" + getFormattedDate(date) + "&7, for '&c" + reason + "&7'!");
                    break;
                }
                default:
                    sendMessage(sender, "Format: /tempbanip <gracz> lub /tempbanip <gracz> <czas>, lub /tempbanip <gracz> <powód> <czas>");
                    break;
            }
            return false;
        }
    }

    private static final class WybaczenieKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String arg1 = args[0];
                if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(arg1)) {
                    sendMessage(sender, "&cERROR: gracz nazwany " + arg1 + " nie jest zbanowany!");
                    return false;
                }
                unban(arg1, false);
                sendMessage(sender, "Wybaczono graczowi &c" + arg1 + "&7!");
                return true;
            } else {
                sendMessage(sender, "Format: /unban <gracz>");
            }
            return false;
        }
    }

    private static final class WybaczenieIPKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                String arg1 = args[0];
                PersistentUserData data = UserFileManager.get(arg1);
                boolean valid = isValidIP(arg1);
                if (data == null && !valid) {
                    sendMessage(sender, "&cNie znaleziono żadnych informacji o graczu " + arg1 + " w bazie danych AlixSystem!");
                    return false;
                }
                String ip = valid ? arg1 : data.getSavedIP();
*//*                if (!Bukkit.getBanList(BanList.Type.IP).isBanned(ip)) {
                    sendMessage(sender, "&cERROR: IP " + ip + " nie jest zbanowane!");
                    return false;
                }*//*
                unban(ip, true);
                sendMessage(sender, "Wybaczono ip &c" + ip + "&7!");
                return true;
            } else {
                sendMessage(sender, "Format: /unbanip <gracz>");
            }
            return false;
        }
    }

    private static final class TymczasowyBanKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 1: {
                    String arg1 = args[0];
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    boolean found = p != null;
                    if (!found)
                        sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy nie dołączył do tego serwer!");
                    String name = found ? p.getName() : arg1;
                    ban(name, "Zostałeś zbanowany!", null, false, sender.getName());
                    broadcastToPermitted("Zbanowano gracza &c" + name + " &7na zawsze!");
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String arg2 = args[1];
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    boolean found = p != null;
                    if (!found) sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy nie dołączył do tego serwer!");
                    String name = found ? p.getName() : arg1;
                    String reason = translateColors(arg2);
                    assert name != null;
                    ban(name, reason, null, false, sender.getName());
                    broadcastToPermitted("Zbanowano gracza &c" + name + " &7za &c" + reason + "&7!");
                    return true;
                }
                case 3: {
                    String arg1 = args[0];
                    String arg3 = args[2];
                    String reason = translateColors(args[1]);
                    OfflinePlayer p = getOfflinePlayer(arg1);
                    boolean found = p != null;
                    if (!found)
                        sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy nie dołączył do tego serwer!");
                    String name = found ? p.getName() : arg1;
                    Date date = getProcessedDate(arg3);
                    assert name != null;
                    ban(name, reason, date, false, sender.getName());
                    broadcastToPermitted("Zbanowano gracza &c" + name + " &7do &c" + getFormattedDate(date) + "&7, za '" + reason + "&7'!");
                    break;
                }
                default:
                    sendMessage(sender, "Format: /tempban <gracz> lub /tempban <gracz> <powód>, lub /tempban <gracz> <powód> <czas>");
                    break;
            }
            return false;
        }
    }


    private static final class UkonczCaptcheKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                UnverifiedUser u = Verifications.get(p);
                if (u == null) {
                    sendMessage(sender, "&cJesteś już zalogowany");
                    return false;
                }
                if (u.hasCompletedCaptcha()) {
                    sendMessage(sender, "&cUkończyłeś już test captcha.");
                    return false;
                }
                if (u.isCaptchaCorrect(args[0])) {
                    u.completeCaptcha();
                    sendMessage(sender, "&aTest captcha został pomyślnie ukończony. Możesz się teraz zarejestrować używając /zarejestruj <hasło>.");
                    return true;
                }
                if (kickOnIncorrectCaptcha) {
                    p.kickPlayer("Nieprawidłowa captcha!");
                    return false;
                }
                sendMessage(sender, "&cNieprawidłowa captcha!");
                return false;
            }
            sendMessage(sender, "Format: /captcha <captcha>");
            return false;
        }
    }

    private static final class ZmianaHaslaKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                User u = getVerifiedUser(p);
*//*                if (u == null) { The Offline Executors should take care of blocking these
                    sendMessage(sender, "&cNajpierw się zaloguj!");
                    return false;
                }
                if (!u.isRegistered()) {
                    sendMessage(sender, "&cNajpierw się zarejestruj!");
                    return false;
                }*//*
                String newPassword = args[0];
                String reason = getInvalidityReason(newPassword, false);
                if (reason == null) {
                    u.changePassword(newPassword);
                    sendMessage(sender, "&aZmiana hasła powiodła się!");
                    return true;
                }
                sendMessage(sender, reason);
                return false;
            }
            sendMessage(sender, "Format: /zmienhaslo <nowe hasło>");
            return false;
        }
    }

    private static final class LogowanieSieKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                UnverifiedUser u = Verifications.get(p);
                if (u == null) {
                    sendMessage(sender, "&cJuż jesteś zalogowany! Aby zmienić hasło proszę użyć /zmienhaslo!");
                    return false;
                }
                if (!u.isRegistered()) {
                    sendMessage(sender, "&cNajpierw się zarejestruj!");
                    return false;
                }
                String password = args[0];
                if (u.isPasswordCorrect(password)) {
                    u.logIn();
                    sendMessage(sender, "&aZalogowanie powiodło się!");
                    return true;
                }
                if (kickOnIncorrectPassword) {
                    p.kickPlayer("Nieprawidłowe hasło!");
                    return false;
                }
                sendMessage(sender, "&cNieprawidłowe hasło!");
                return false;
            }
            sendMessage(sender, "Format: /zaloguj <haslo>");
            return false;
        }
    }

    private static final class ZarajestrujSieKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 1) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                UnverifiedUser u = Verifications.get(p);
                if (!u.hasCompletedCaptcha()) {
                    sendMessage(sender, "&cNajpierw ukończ captchę!");
                    return false;
                }
                if (u == null || u.isRegistered()) {
                    sendMessage(sender, "&cJesteś już zarejestrowany!");
                    return false;
                }
                String arg1 = args[0];
                String reason = getInvalidityReason(arg1, false);
                if (reason == null) {
                    if (u.register(arg1)) sendMessage(sender, "&aPoprawnie zarejestrowano twoje hasło!");
                    else sendMessage(sender, "Osiągnąłeś limit kont (" + maximumTotalAccounts + ")!");
                    return true;
                }
                sendMessage(sender, reason);
                return false;
            }
            sendMessage(sender, "Format: /zarejestruj <haslo>");
            return false;
        }
    }

    private static final class ZmianaNazwyKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            if (label.equalsIgnoreCase("nicknameplayer")) {
                if (l == 0) {
                    sendMessage(sender, "Format: /nicknameplayer <gracz> <nazwa>");
                    return false;
                }
                String arg1 = args[0];
                Player p = Bukkit.getPlayerExact(arg1);
                if (p == null) {
                    sendMessage(sender, "&cNie znaleziono gracza o nazwie " + arg1 + "!");
                    return false;
                }
                if (l == 1) {
                    setName(p, null);
                    sendMessage(sender, "Zresetowano nazwę dla gracza " + p.getName() + "!");
                    return false;
                }
                String nickname = translateColors(mergeWithSpacesAndSkip(args, 1));
                setName(p, nickname);
                sendMessage(sender, "Ustawiono graczowi " + p.getName() + " nazwę '" + nickname + "&7'.");
                return true;
            }
            if (l == 0) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                setName(p, null);
                sendMessage(sender, "Zresetowano twoją nazwę!");
                return true;
            }
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            String nickname = translateColors(setAsOneAndAddAfter(args, " "));
            setName(p, nickname);
            sendMessage(sender, "Ustawiono twoją nazwę na '" + nickname + "&7'.");
            return true;
//sendMessage(sender, "Format: /nickname lub /nickname <nazwa>, lub /nicknameplayer <gracz> <nazwa>");
            //return false;
        }
    }

    private static final class ListaGraczyOnlineKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            Collection<? extends Player> onlines = Bukkit.getOnlinePlayers();
            int l = onlines.size();
            if (l == 0) {
                sendMessage(sender, "Nie znaleziono żadnych graczy online!");
                return false;
            }
            sendMessage(sender, "Lista graczy online (" + l + " Obecnie Online) \n" + getListOfAllOnlinePlayers(onlines));
            return true;
        }
    }

    private static final class ZmianaTrybuGryKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    switch (label) {
                        case "gm0":
                        case "gms":
                            p.setGameMode(GameMode.SURVIVAL);
                            sendMessage(sender, "Twój tryb gry został zmieniony na &cprzetrwanie.");
                            break;
                        case "gm1":
                        case "gmc":
                            p.setGameMode(GameMode.CREATIVE);
                            sendMessage(sender, "Twój tryb gry został zmieniony na &ckreatywny.");
                            break;
                        case "gm2":
                        case "gma":
                            p.setGameMode(GameMode.ADVENTURE);
                            sendMessage(sender, "Twój tryb gry został zmieniony na &cprzygodę.");
                            break;
                        case "gm3":
                        case "gmsp":
                            p.setGameMode(GameMode.SPECTATOR);
                            sendMessage(sender, "Twój tryb gry został zmieniony na &cwidza.");
                            break;
                        default:
                            sendMessage(sender, "&cNieznany tryb gry!");
                            break;
                    }
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p2 = Bukkit.getPlayerExact(arg1);
                    if (p2 != null) {
                        String name = p2.getName();
                        switch (label) {
                            case "gm0":
                            case "gms":
                                p2.setGameMode(GameMode.SURVIVAL);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &cprzetrwanie.");
                                break;
                            case "gm1":
                            case "gmc":
                                p2.setGameMode(GameMode.CREATIVE);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &ckreatywny.");
                                break;
                            case "gm2":
                            case "gma":
                                p2.setGameMode(GameMode.ADVENTURE);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &cprzygodę.");
                                break;
                            case "gm3":
                            case "gmsp":
                                p2.setGameMode(GameMode.SPECTATOR);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &cwidza.");
                                break;
                            default:
                                sendMessage(sender, "&cNieznany tryb gry!");
                                break;
                        }
                    } else if (!isConsoleButPlayerRequired(sender)) {
                        Player p = (Player) sender;
                        if (isNumber(arg1)) {
                            int gamemode = Integer.parseInt(arg1);
                            switch (gamemode) {
                                case 0:
                                    p.setGameMode(GameMode.SURVIVAL);
                                    sendMessage(sender, "Twój tryb gry został zmieniony na &cprzetrwanie.");
                                    break;
                                case 1:
                                    p.setGameMode(GameMode.CREATIVE);
                                    sendMessage(sender, "Twój tryb gry został zmieniony na &ckreatywny.");
                                    break;
                                case 2:
                                    p.setGameMode(GameMode.ADVENTURE);
                                    sendMessage(sender, "Twój tryb gry został zmieniony na &cprzygodę.");
                                    break;
                                default:
                                    p.setGameMode(GameMode.SPECTATOR);
                                    sendMessage(sender, "Twój tryb gry został zmieniony na &cwidza.");
                                    break;
                            }
                        } else {
                            GameMode gamemode = getGameModeType(arg1);
                            p.setGameMode(gamemode);
                            sendMessage(sender, "Twój tryb gry został zmieniony na &c" + gamemode.toString().toLowerCase() + ".");
                        }
                    }
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String arg2 = args[1];
                    Player p = Bukkit.getPlayerExact(arg2);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg2 + "!");
                        return false;
                    }
                    String name = p.getName();
                    if (isNumber(arg1)) {
                        int gamemode = Integer.parseInt(arg1);
                        switch (gamemode) {
                            case 0:
                                p.setGameMode(GameMode.SURVIVAL);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &cprzetrwanie.");
                                break;
                            case 1:
                                p.setGameMode(GameMode.CREATIVE);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &ckreatywny.");
                                break;
                            case 2:
                                p.setGameMode(GameMode.ADVENTURE);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &cprzygodę.");
                                break;
                            default:
                                p.setGameMode(GameMode.SPECTATOR);
                                sendMessage(sender, "Tryb gry dla gracza " + name + " został zmieniony na &cwidza.");
                                break;
                        }
                    } else {
                        GameMode gamemode = getGameModeType(arg1);
                        p.setGameMode(gamemode);
                        sendMessage(sender, "Tryb gry dla gracza " + name + " został ustawiony na &c" + gamemode.toString().toLowerCase() + ".");
                    }
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /gm <trybgry> lub /gm <trybgry> <gracz>");
                    break;
            }
            return false;
        }
    }

    private static final class ZobaczEkwipunekKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            if (args.length == 1) {
                if (isConsoleButPlayerRequired(sender)) return false;
                String arg1 = args[0];
                Player p = (Player) sender;
                Player p2 = Bukkit.getPlayerExact(arg1);
                if (p == null) {
                    sendMessage(sender, "&cNie znaleziono gracza " + arg1 + "!");
                    return false;
                }
                p.openInventory(p2.getInventory());
                return true;
            } else sendMessage(sender, "Format: /ekwipunek <gracz>");
            return false;
        }
    }

    private static final class SkrzyniaKresuKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    p.openInventory(p.getEnderChest());
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = (Player) sender;
                    Player p2 = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg1 + "!");
                        return false;
                    }
                    p.openInventory(p2.getEnderChest());
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /enderchest lub /enderchest <gracz>");
                    break;
            }
            return false;
        }
    }

    private static final class NajedzKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    p.setFoodLevel(20);
                    sendMessage(sender, "Zostałeś nasycony!");
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg1 + "!");
                        return false;
                    }
                    p.setFoodLevel(20);
                    sendMessage(sender, "Gracz " + p.getName() + " został nasycony!");
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /najedz lub /najedz <gracz>");
                    break;
            }
            return false;
        }
    }

*//*    private static final class ZniknijKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    User u = getUser(p);
                    if (u.isVanished()) {
                        u.setVanished(false);
                        sendMessage(sender, "&cStałeś się widoczny!");
                    } else {
                        u.setVanished(true);
                        sendMessage(sender, "&aStałeś się niewidoczny!");
                    }
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg1 + "!");
                        return false;
                    }
                    String name = p.getName();
                    User u = getUser(p);
                    if (u.isVanished()) {
                        u.setVanished(false);
                        sendMessage(sender, "&cGracz " + name + " stałe się widoczny!");
                    } else {
                        u.setVanished(true);
                        sendMessage(sender, "&aGracz " + name + " stał się niewidoczny!");
                    }
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /zniknij lub /zniknij <gracz>");
                    break;
            }
            return false;
        }
    }*//*

    private static final class UleczKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    Collection<PotionEffect> potions = p.getActivePotionEffects();
                    for (PotionEffect pe : potions) p.removePotionEffect(pe.getType());
                    p.setHealth(20);
                    p.setFoodLevel(20);
                    p.setFireTicks(0);
                    sendMessage(sender, "Zostałeś uleczony!");
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg1 + "!");
                        return false;
                    }
                    String name = p.getName();
                    Collection<PotionEffect> potions = p.getActivePotionEffects();
                    for (PotionEffect pe : potions) p.removePotionEffect(pe.getType());
                    p.setHealth(20);
                    p.setFoodLevel(20);
                    p.setFireTicks(0);
                    sendMessage(sender, "Gracz " + name + " został uleczony!");
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /ulecz lub /ulecz <gracz>");
                    break;
            }
            return false;
        }
    }

    private static final class PredkoscKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

            int l = args.length;
            switch (l) {
                case 1: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    String arg1 = args[0];
                    if (!isNumber(arg1)) {
                        sendMessage(sender, "&c'" + arg1 + "' nie jest liczbą!");
                        return false;
                    }
                    float n = Float.parseFloat(arg1);
                    if (p.isFlying()) {
                        p.setFlySpeed(clampOfOne(n * doubledDefaultFlySpeed));
                        sendMessage(sender, "Twoja &cPrędkość Latania &7została ustawiona na &c" + setAsClearNumber(n));
                    } else {
                        p.setWalkSpeed(clampOfOne(n * doubledDefaultWalkSpeed));
                        sendMessage(sender, "Twoja &cPrędkość Chodzenia &7została ustawiona na &c" + setAsClearNumber(n));
                    }
                    return true;
                }
                case 2: {
                    String arg1 = args[0];
                    String arg2 = args[1];
                    Player p = Bukkit.getPlayerExact(arg2);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg2 + "!");
                        return false;
                    }
                    String name = p.getName();

                    if (!isNumber(arg1)) {
                        sendMessage(sender, "&c'" + arg1 + "' nie jest liczbą!");
                        return false;
                    }
                    float n = Float.parseFloat(arg1);
                    if (p.isFlying()) {
                        p.setFlySpeed(clampOfOne(n * doubledDefaultFlySpeed));
                        sendMessage(sender, "&cPrędkość Latania &7dla " + name + " została ustawiona na &c" + setAsClearNumber(n));
                    } else {
                        p.setWalkSpeed(clampOfOne(n * doubledDefaultWalkSpeed));
                        sendMessage(sender, "&cPrędkość Chodzenia &7dla " + name + " została ustawiona na &c" + setAsClearNumber(n));
                    }
                    return true;
                }
                case 3: {
                    String arg1 = args[0];
                    String arg2 = args[1];
                    String arg3 = args[2];
                    Player p = Bukkit.getPlayerExact(arg2);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg2 + "!");
                        return false;
                    }
                    String name = p.getName();

                    if (!isNumber(arg1)) {
                        sendMessage(sender, "&c'" + arg1 + "' nie jest liczbą!");
                        return false;
                    }
                    float n = Float.parseFloat(arg1);
                    switch (arg3.toLowerCase()) {
                        case "latanie":
                            p.setFlySpeed(clampOfOne(n * doubledDefaultFlySpeed));
                            sendMessage(sender, "&cPrędkość Latania &7dla " + name + " została ustawiona na &c" + setAsClearNumber(n));
                            break;
                        case "chodzenie":
                            p.setWalkSpeed(clampOfOne(n * doubledDefaultWalkSpeed));
                            sendMessage(sender, "&cPrędkość Chodzenia &7dla " + name + " została ustawiona na &c" + setAsClearNumber(n));
                            break;
                        default:
                            sendMessage(sender, "&cCzwarty arugment powinien być 'chodzenie' lub 'latanie', jednak otrzymano" + arg3 + "'!");
                            break;
                    }
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /predkosc <liczba> lub /predkosc <liczba> <gracz>, lub /predkosc <liczba> <gracz> (chodzenie/latanie)");
                    break;
            }
            return false;
        }
    }

    private static final class NazwijPrzedmiotKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 0) {
                if (isConsoleButPlayerRequired(sender)) return false;
                Player p = (Player) sender;
                ItemStack i = p.getInventory().getItemInMainHand();
                ItemMeta im = i.getItemMeta();
                if (im == null) {
                    sendMessage(sender, "&cMusisz trzymać przedmiot który chesz nazwać!");
                    return false;
                }
                im.setDisplayName(null);
                i.setItemMeta(im);
                sendMessage(sender, "&6Poprawnie zresetowano ostatnią nazwę trzymanego przedmiotu!");
                return true;
            }
            if (isConsoleButPlayerRequired(sender)) return false;
            Player p = (Player) sender;
            ItemStack i = p.getInventory().getItemInMainHand();
            ItemMeta im = i.getItemMeta();
            if (im == null) {
                sendMessage(sender, "&cMusisz trzymać przedmiot który chesz nazwać!");
                return false;
            }
            im.setDisplayName(translateColors(setAsOneAndAddAfter(args, " ")));
            i.setItemMeta(im);
            sendMessage(sender, "&6Poprawnie nazwano przedmiot.");
            return false;
        }
    }

    private static final class LatanieKomenda implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            int l = args.length;
            switch (l) {
                case 0: {
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player p = (Player) sender;
                    if (p.isFlying()) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        sendMessage(sender, "Latanie zostało &cwyłączone!");
                    } else {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                        sendMessage(sender, "Latanie zostało &awłączone!");
                    }
                    return true;
                }
                case 1: {
                    String arg1 = args[0];
                    Player p = Bukkit.getPlayerExact(arg1);
                    if (p == null) {
                        sendMessage(sender, "&cNie znaleziono gracza " + arg1 + "!");
                        return false;
                    }
                    String name = p.getName();
                    if (p.isFlying()) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        sendMessage(sender, "Latanie dla gracza zostało " + name + " &cwyłączone!");
                    } else {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                        sendMessage(sender, "Latanie dla gracza zostało " + name + " &awłączone!");
                    }
                    return true;
                }
                default:
                    sendMessage(sender, "Format: /latanie lub /latanie <gracz>");
                    break;
            }
            return false;
        }
    }
*/
}