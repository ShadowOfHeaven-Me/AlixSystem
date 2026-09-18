package shadow.systems.commands;

import alix.common.antibot.algorithms.any.PanicModeManager;
import alix.common.antibot.epoll.Telemetry;
import alix.common.antibot.epoll.TelemetryProfiler;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.antibot.firewall.ataraxia.AlixAtaraxia;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.PremiumStatus;
import alix.common.data.security.password.Password;
import alix.common.database.DatabaseUpdater;
import alix.common.database.migrate.MigrateManager;
import alix.common.database.migrate.MigrateType;
import alix.common.environment.ServerEnvironment;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.AlixMessage;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.throwable.AlixError;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import shadow.systems.commands.alix.ABStats;
import shadow.systems.login.autoin.premium.SpigotEncryption;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.UserManager;
import shadow.utils.users.Verifications;

import java.net.InetAddress;
import java.util.Date;

import static shadow.utils.main.AlixUtils.*;

public final class AdminAlixCommands implements CommandExecutor {

    private final String passwordResetMessage = Messages.get("password-reset-forcefully");
    private final AlixMessage playerDataNotFound = Messages.getAsObject("player-data-not-found");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int l = args.length;
        if (l > 0) {
            String arg1 = args[0].toLowerCase();
            if (l > 1) {
                String arg2 = args[1];
                switch (arg1) {
                    case "ufw": {
                        InetAddress ip;
                        try {
                            //assumes the user does not input a resolvable domain (cuz that would block, not great)
                            ip = InetAddress.getByName(arg2);
                        } catch (Exception e) {
                            sendMessage(sender, Messages.get("as-ufw-invalid-ip", arg2));
                            return false;
                        }

                        if (FireWallManager.removeDynamic(ip))
                            sendMessage(sender, Messages.get("as-ufw-removed", arg2));
                        else {
                            if (PanicModeManager.isBlocked(ip))
                                sendMessage(sender, Messages.get("as-ufw-not-firewalled-panicmode", arg2));
                            else if (FireWallManager.isBlocked0(ip)) {
                                sendMessage(sender, Messages.get("as-ufw-blocked-static", arg2));
                            } else {
                                sendMessage(sender, Messages.get("as-ufw-not-firewalled", arg2));
                            }
                        }
                        break;
                    }
                    case "migrate": {
                        MigrateType type;
                        try {
                            type = MigrateType.valueOf(arg2.toUpperCase());
                        } catch (Exception e) {
                            sendMessage(sender, Messages.get("as-migrate-invalid-type", arg2));
                            return false;
                        }
                        AlixScheduler.asyncBlocking(() -> MigrateManager.migrate(type));
                        break;
                    }
                    case "bl":
                    case "bypasslist":
                    case "bypasslimit": {
                        if (AllowListFileManager.has(arg2)) {
                            sendMessage(sender, Messages.get("as-bypasslimit-already-added", arg2));
                            return true;
                        }
                        AllowListFileManager.add(arg2);
                        sendMessage(sender, Messages.get("as-bypasslimit-added", arg2));
                        break;
                    }
                    case "bl-r":
                    case "bypasslist-remove":
                    case "bypasslimit-remove": {
                        if (AllowListFileManager.remove(arg2)) {
                            sendMessage(sender, Messages.get("as-bypasslimit-removed", arg2));
                            return true;
                        }
                        sendMessage(sender, Messages.get("as-bypasslimit-not-added", arg2));
                        break;
                    }

                    case "frd":
                    case "fullyremovedata": {
                        PersistentUserData data = UserFileManager.remove(arg2);

                        if (AllowListFileManager.remove(arg2)) {
                            sendMessage(sender, Messages.get("as-frd-removed-from-allowlist", arg2));
                        }

                        //todo: Also remove from UserTokensFileManager?
                        if (data == null) {
                            sendMessage(sender, playerDataNotFound.format(arg2));
                            return false;
                        }
                        sendMessage(sender, Messages.get("as-frd-success", arg2));
                    }
                    break;

                    case "rs":
                    case "resetstatus": {
                        PersistentUserData data = UserFileManager.get(arg2);

                        if (data == null) {
                            sendMessage(sender, playerDataNotFound.format(arg2));
                            return false;
                        }
                        data.setPremiumData(PremiumData.UNKNOWN);

                        sendMessage(sender, Messages.get("as-resetstatus-success", arg2));
                        break;
                    }
                    case "fs":
                    case "forcestatus": {
                        if (l != 3) {
                            sendMessage(sender, Messages.get("as-forcestatus-format"));
                            return false;
                        }
                        PersistentUserData data = UserFileManager.get(arg2);

                        if (data == null) {
                            sendMessage(sender, playerDataNotFound.format(arg2));
                            return false;
                        }

                        String arg3 = args[2];
                        PremiumStatus status;
                        try {
                            status = PremiumStatus.valueOf(arg3.toUpperCase());
                        } catch (Exception e) {
                            sendMessage(sender, Messages.get("as-forcestatus-invalid-status"));
                            return false;
                        }

                        if (data.getPremiumData().getStatus() == status) {
                            sendMessage(sender, Messages.get("as-forcestatus-already-has-status", arg2, status));
                            return false;
                        }

                        if (!status.isPremium()) {
                            data.setPremiumData(status.isNonPremium() ? PremiumData.NON_PREMIUM : PremiumData.UNKNOWN);
                            sendMessage(sender, Messages.get("as-forcestatus-success", arg2, status));
                            return true;
                        }

                        var cached = PremiumDataCache.getOrUnknown(arg2);
                        if (cached.getStatus().isKnown()) {
                            if (cached.getStatus().isPremium()) {
                                data.setPremiumData(cached);
                                sendMessage(sender, Messages.get("as-forcestatus-success", arg2, cached.getStatus()));
                                return true;
                            }
                            sendMessage(sender, Messages.get("as-forcestatus-cached-non-premium", arg2));
                            return false;
                        }
                        var player = Bukkit.getPlayer(arg2);
                        if (player != null && !Verifications.has(player)) {
                            var packetUUID = AlixChannelHandler.getLoginAssignedUUID(player);
                            if (packetUUID != null && packetUUID.version() != 4) {
                                sendMessage(sender, Messages.get("as-forcestatus-self-declared-non-premium", arg2));
                                return false;
                            }
                        }
                        PremiumUtils.getOrRequestAndCacheData(null, arg2, newPremiumData -> {
                            if (newPremiumData.getStatus().isUnknown()) {
                                sendMessage(sender, Messages.get("as-forcestatus-unknown", arg2));
                                return;
                            }
                            if (newPremiumData.getStatus().isNonPremium()) {
                                sendMessage(sender, Messages.get("as-forcestatus-api-non-premium", arg2));
                                return;
                            }
                            data.setPremiumData(newPremiumData);
                            sendMessage(sender, Messages.get("as-forcestatus-success-api", arg2, newPremiumData.premiumUUID()));
                        });
                        break;
                    }
                    case "rf":
                    case "registerforcefully": {
                        if (UserFileManager.hasName(arg2)) {
                            sendMessage(sender, Messages.get("as-registerforcefully-already-exists", arg2));
                            return false;
                        }

                        if (l == 2) {
                            sendMessage(sender, Messages.get("as-specify-password"));
                            return false;
                        }

                        String password = args[2];
                        LoginType type;
                        if (l >= 4) {
                            String arg4 = args[3];
                            try {
                                type = LoginType.from(arg4.toUpperCase(), false, false);
                            } catch (Exception e) {
                                sendMessage(sender, Messages.get("as-invalid-login-type", arg4));
                                return false;
                            }
                        } else type = LoginType.COMMAND;

                        AlixUtils.getPasswordInvalidityReasonAsync(password, type, invalidityReason -> {
                            if (invalidityReason != null) {
                                sendMessage(sender, Messages.get("as-invalid-password-prefix"));
                                sender.sendMessage(invalidityReason);
                                return;
                            }

                            PersistentUserData data = PersistentUserData.createDefault(arg2, PersistentUserData.UNKNOWN_IP, Password.fromUnhashed(password));

                            data.setLoginType(type);
                            String passFormatted = "*".repeat(password.length() - 3) + password.substring(password.length() - 3);
                            sendMessage(sender, Messages.get("as-registerforcefully-success", data.getName(), passFormatted, type));
                        });
                        return true;
                    }
                    case "cp":
                    case "changepassword": {
                        if (l == 2) {
                            sendMessage(sender, Messages.get("as-specify-password"));
                            return false;
                        }
                        PersistentUserData data = UserFileManager.get(arg2);

                        if (data == null) {
                            sendMessage(sender, playerDataNotFound.format(arg2));
                            return false;
                        }

                        String password = args[2];
                        LoginType type;
                        if (l >= 4) {
                            String arg4 = args[3];
                            try {
                                type = LoginType.from(arg4.toUpperCase(), false, false);
                            } catch (Exception e) {
                                sendMessage(sender, Messages.get("as-invalid-login-type", arg4));
                                return false;
                            }
                        } else type = data.getLoginType();

                        AlixUtils.getPasswordInvalidityReasonAsync(password, type, invalidityReason -> {
                            if (invalidityReason != null) {
                                sendMessage(sender, Messages.get("as-invalid-password-prefix"));
                                sender.sendMessage(invalidityReason);
                                return;
                            }

                            data.setPassword(password);
                            data.setLoginType(type);
                            String passFormatted = "*".repeat(password.length() - 3) + password.substring(password.length() - 3);
                            sendMessage(sender, Messages.get("as-changepassword-success", data.getName(), passFormatted, type));

                            if (data.getLoginParams().getExtraLoginType() != null) {
                                sendMessage(sender, Messages.get("as-changepassword-extra-login-cleared"));
                                data.getLoginParams().setExtraLoginType(null);
                            }
                        });
                        return true;
                    }
                    case "rp":
                    case "resetpassword": {
                        PersistentUserData data = UserFileManager.get(arg2);

                        if (data == null) {
                            sendMessage(sender, playerDataNotFound.format(arg2));
                            return false;
                        }

                        data.resetPasswords();

                        Player p = Bukkit.getPlayerExact(data.getName());
                        if (p != null) p.kickPlayer(passwordResetMessage);

                        if (l > 2) {
                            String arg3 = args[2];

                            LoginType type;

                            try {
                                type = LoginType.from(arg3.toUpperCase(), false, false);
                            } catch (Exception e) {
                                sendMessage(sender, Messages.get("as-invalid-login-type", arg3));
                                return false;
                            }

                            data.setLoginType(type);
                            sendMessage(sender, Messages.get("as-resetpassword-success-with-type", arg2, type));
                        } else sendMessage(sender, Messages.get("as-resetpassword-success", arg2));
                    }
                    break;
                    case "user": {
                        AlixScheduler.async(() -> {
                            OfflinePlayer offlinePlayer = getOfflinePlayer(arg2);
                            if (offlinePlayer == null) {
                                sendMessage(sender, Messages.get("error-player-never-joined", arg2));
                                return;
                            }
                            PersistentUserData data = UserFileManager.get(offlinePlayer.getName());
                            if (data == null) {
                                sendMessage(sender, playerDataNotFound.format(arg2));
                                return;
                            }

                            var channel = SpigotEncryption.channel(data);

                            boolean dVer = data.getLoginParams().isDoubleVerificationEnabled();
                            sendMessage(sender, "");
                            sendMessage(sender, Messages.get("as-user-header", offlinePlayer.getName()));
                            sendMessage(sender, Messages.get("as-user-ip", data.getSavedIP().getHostAddress()));
                            sendMessage(sender, Messages.get("as-user-premium-status", data.getPremiumData().getStatus().readableName()));

                            if (Telemetry.ENABLED && channel != null) {
                                var sig = TelemetryProfiler.synSignature(channel);
                                if (sig != null) {
                                    sendMessage(sender, Messages.get("as-user-os", sig.os.getReadableName()));
                                    sendMessage(sender, Messages.get("as-user-connection-env", sig.mtuEnv.getReadableName()));
                                }
                            }

                            if (!data.getSavedIP().equals(PersistentUserData.UNKNOWN_IP)) {//I guess possibly incorrect info when testing on localhost
                                var accounts = UserFileManager.getAllData().stream().filter(d -> d.getSavedIP().equals(data.getSavedIP()))
                                        .map(PersistentUserData::getName).toList();

                                var extraInfo = accounts.size() > 1 ? " &7(" + String.join(", ", accounts) + ")" : "";
                                sendMessage(sender, Messages.get("as-user-accounts", accounts.size(), extraInfo));
                            }

                            if (ServerEnvironment.isPaper()) {
                                var cached = Bukkit.getOfflinePlayerIfCached(arg2);
                                if (cached != null)
                                    sendMessage(sender, Messages.get("as-user-uuid-version", cached.getUniqueId().version()));
                            }

                            var isEncrypted = SpigotEncryption.isOnlineEncryptionEnabled(channel);
                            if (isEncrypted != null)
                                sendMessage(sender, Messages.get("as-user-encryption", isEncrypted ? "Enabled" : "Disabled"));

                            boolean isPremium = data.getPremiumData().getStatus().isPremium();
                            if (!isPremium) {
                                sendMessage(sender, Messages.get("as-user-ip-autologin", data.getLoginParams().getIpAutoLogin() ? "Enabled" : "Disabled"));
                                sendMessage(sender, Messages.get("as-user-login-type", data.getLoginType()));
                                if (dVer)
                                    sendMessage(sender, Messages.get("as-user-second-login-type", data.getLoginParams().getExtraLoginType()));

                                sendMessage(sender, Messages.get("as-user-double-verification", dVer ? "Enabled" : "Disabled"));

                                String authApp;
                                switch (data.getLoginParams().getAuthSettings()) {
                                    case PASSWORD:
                                        authApp = Messages.get("as-user-auth-app-disabled");
                                        break;
                                    case AUTH_APP:
                                        authApp = Messages.get("as-user-auth-app-only");
                                        break;
                                    case PASSWORD_AND_AUTH_APP:
                                        authApp = dVer ? Messages.get("as-user-auth-app-and-passwords") : Messages.get("as-user-auth-app-and-password");
                                        break;
                                    default:
                                        throw new AlixError("Invalid - " + data.getLoginParams().getAuthSettings());
                                }
                                sendMessage(sender, Messages.get("as-user-totp", authApp));
                                sendMessage(sender, Messages.get("as-user-totp-linked", data.getLoginParams().hasProvenAuthAccess() ? "&aYep" : "&cNope"));
                            }
                            long createdAt = data.createdAt();
                            long firstPlayed = offlinePlayer.getFirstPlayed();

                            long older = firstPlayed == 0 ? createdAt : Math.min(createdAt, firstPlayed);
                            sendMessage(sender, Messages.get("as-user-first-joined", getFormattedDate(new Date(older))));
                            //sendMessage(sender, (offlinePlayer.isOnline() ? "Currently online from: &c" : "Last joined: &c") + getFormattedDate(new Date(offlinePlayer.getLastPlayed())));
                            sendMessage(sender, "");
                        });
                        return true;
                    }
                    case "valueof":
                        if (isNumber(arg2)) sendMessage(sender, setAsClearNumber(arg2));
                        else sendMessage(sender, Messages.get("as-valueof-expected-number"));
                        break;
                    case "calc":
                    case "calculate":
                        String toCalculate = setAsOne(skipArray(args, 1));
                        String replacedConstants = toCalculate.toLowerCase().replaceAll("random", String.valueOf(random.nextDouble())).
                                replaceAll("omega", "0.56714329040978").replaceAll("alfa", "2.502907875095892").
                                replaceAll("ipsylon", "4.66920160902990").replaceAll("pi", "3.141592653589793").
                                replaceAll("e", "2.718281828459045").replaceAll("k", "2.584981759579253").
                                replaceAll("f", "2.807770242028519");
                        String result;
                        try {
                            result = setAsClearNumber(eval(replacedConstants));
                        } catch (NumberFormatException e) {
                            sendMessage(sender, e.getMessage());
                            return false;
                        }
                        sendMessage(sender, Messages.get("as-calc-result", toCalculate, result));
                        break;
                    case "avg":
                    case "average":
                        String[] array = skipArray(args, 1);
                        String entirety = setAsOne(array);
                        String r = setAsClearNumber(eval(entirety) / array.length);
                        sendMessage(sender, Messages.get("as-avg-result", entirety, array.length, r));
                        break;
                    case "forceop": {
                        if (!(sender instanceof ConsoleCommandSender)) {
                            sendMessage(sender, Messages.get("as-forceop-console-only"));
                            return false;
                        }
                        OfflinePlayer p = getOfflinePlayer(arg1);
                        if (p == null || p.getName() == null) {
                            sendMessage(sender, Messages.get("warning-player-never-joined", arg1));
                            AlixHandler.handleOperatorSet(sender, arg1);
                            return false;
                        }
                        AlixHandler.handleOperatorSet(sender, p.getName());
                        break;
                    }
                    case "forcedeop": {
                        if (!(sender instanceof ConsoleCommandSender)) {
                            sendMessage(sender, Messages.get("as-forceop-console-only"));
                            return false;
                        }
                        OfflinePlayer p = getOfflinePlayer(arg1);
                        if (p == null || p.getName() == null) {
                            sendMessage(sender, Messages.get("warning-player-never-joined", arg1));
                            AlixHandler.handleOperatorUnset(sender, arg1);
                            return false;
                        }
                        AlixHandler.handleOperatorUnset(sender, p.getName());
                        break;
                    }
/*                    case "median":
                        String[] a = skipArray(args, 1);
                        String b = setAsOne(a);
                        String[] c = split(b.replaceAll(" ",""),',');
                        dispatch(sender, "median(" + b + ") = " + getMedian(parseArray(c)));
                        break;*/
/*                    case "item":
                        if (bukkitVersion < 16) {
                            sendMessage(sender, "&cThis command is available on 1.16+ server version, but current server version is " + serverVersion + ".");
                            return false;
                        }
                        switch (arg2) {
                            case "1":
                                //Dealing damage heals hp
                                dispatchServerCommand("/give " + sender.getName() + " netherite_shovel{Unbreakable:1b,AttributeModifiers:[{AttributeName:\"generic.attack_damage\"" + "," +
                                        "Amount:5,Slot:mainhand,Name:\"generic.attack_damage\",UUID:[I;-122328,10071,202313,-20142]}]," +
                                        "display:{Name:'[{\"text\":\"Spuit Pipette\",\"italic\":false,\"bold\":true,\"color\":\"red\"}]'," +
                                        "Lore:['[{\"text\":\"Heals the same amount of damage it\",\"italic\":false,\"color\":\"gray\"}]','" +
                                        "[{\"text\":\"dealt.\",\"italic\":false,\"color\":\"gray\"}]']},HideFlags:4} 1");
                                break;
                            case "2":
                                //Deals 2.5x greater damage every third hit
                                dispatchServerCommand("/give " + sender.getName() + " netherite_axe{Unbreakable:1,AttributeModifiers:[{AttributeName:\"generic.knockback_resistance" + "\"," +
                                        "Amount:1,Slot:mainhand,Name:\"generic.knockback_resistance\",UUID:[I;-122328,23571,202313,-47142]}]" + "," +
                                        "display:{Name:'[{\"text\":\"Axe of Cruelty\",\"italic\":false,\"bold\":true,\"color\":\"red\"}]'," +
                                        "Lore:['[{\"text\":\"Nullifies knockback, and slows\",\"italic\":false,\"color\":\"gray\"}]','" +
                                        "[{\"text\":\"down your enemies.\",\"italic\":false,\"color\":\"gray\"}]']},HideFlags:4} 1");
                                break;
                            case "3":
                                //Ignores shields and summons fireballs
                                dispatchServerCommand("/give " + sender.getName() + " bow{Unbreakable:1," +
                                        "display:{Name:'[{\"text\":\"Houyi\\'s Bow\",\"italic\":false,\"color\":\"gold\",\"bold\":true}]'}," +
                                        "Enchantments:[{id:flame,lvl:7},{id:power,lvl:7}],HideFlags:5} 1");
                                break;
                        }*/
                    default:
                        sendMessage(sender, Messages.get("as-unknown-command"));
                        break;
                }
                return true;
            }
            switch (arg1) {
                case "helpmath"://ufw
                    sendMessage(sender, "");
                    sendMessage(sender, Messages.get("as-help-math"));
                    sendMessage(sender, Messages.get("as-help-avg"));
                    sendMessage(sender, Messages.get("as-help-valueof"));
                    sendMessage(sender, Messages.get("as-help-constants"));
                    sendMessage(sender, Messages.get("as-help-randommath", AlixUtils.getRandomMathematicalOperation()));
                    sendMessage(sender, "");
                    break;
                case "help":
                    sendMessage(sender, "");
                    sendMessage(sender, Messages.get("admin-commands-header"));
                    sendMessage(sender, Messages.get("admin-commands-section-accounts"));
                    sendMessage(sender, Messages.get("as-help-user"));
                    sendMessage(sender, Messages.get("as-help-bypasslimit"));
                    sendMessage(sender, Messages.get("as-help-bypasslimit-remove"));
                    sendMessage(sender, Messages.get("as-help-resetpassword"));
                    sendMessage(sender, Messages.get("as-help-registerforcefully"));
                    sendMessage(sender, Messages.get("as-help-changepassword"));
                    sendMessage(sender, Messages.get("as-help-resetstatus"));
                    sendMessage(sender, Messages.get("as-help-forcestatus"));
                    sendMessage(sender, Messages.get("as-help-fullyremovedata"));
                    sendMessage(sender, "");
                    sendMessage(sender, Messages.get("admin-commands-section-server"));
                    sendMessage(sender, Messages.get("as-help-info"));
                    sendMessage(sender, Messages.get("as-help-abstats"));
                    sendMessage(sender, Messages.get("as-help-helpmath"));
                    if (isOperatorCommandRestricted) {
                        sendMessage(sender, "");
                        sendMessage(sender, Messages.get("admin-commands-section-operators"));
                        sendMessage(sender, Messages.get("as-help-forceop"));
                        sendMessage(sender, Messages.get("as-help-forcedeop"));
                    }
                    sendMessage(sender, "");
/*                        dispatch(sender, "&c/as median <numbers> &7- " +
                                "Returns the median of the given numbers. " +
                                "Example: &c/as median 5, 2, 3, 6, 4 returns 3.");*/
                    /*sendMessage(sender, "&c/as incognito <skin> &7- Turns your skin to a skin of a player you named, and gives you a random name.");
                    sendMessage(sender, "&c/as incognito <skin> <true/false> &7- Turns your skin to a skin of a player you named, " +
                            "and gives you a name, which, if set to true, is english, and else its polish.");
                    sendMessage(sender, "&c/as incognito <skin> <true/false> <true/false> &7- Turns your skin to a skin of a player you named, " +
                            "and gives you a name, in which: first true/false statement defines if it should be in english or in polish, " +
                            "and second statement defines if it should an actual name or a name of a thing.");*/
                    //sendMessage(sender, "&c/as pings &7- Returns a list of all contained ip adresses that have pinged this server.");
                    //sendMessage(sender, "&c/as incognitooff &7- Gives you back your original name");//, and skin.");

                    break;

                case "profilejoins": {
                    LimboJoinProfiler.PROFILE_JOINS = !LimboJoinProfiler.PROFILE_JOINS;

                    if (LimboJoinProfiler.PROFILE_JOINS)
                        sendMessage(sender, Messages.get("as-profilejoins-enabled"));
                    else
                        sendMessage(sender, Messages.get("as-profilejoins-disabled"));
                    return true;
                }
                case "save_all_local_to_db": {
                    sendMessage(sender, Messages.get("as-save-all-local-to-db"));
                    UserFileManager.saveLocalToDb();
                    return true;
                }
                case "testdb": {
                    DatabaseUpdater.INSTANCE.testDatabase();
                    return true;
                }
                case "__reset_all_premium_passwords": {
                    UserFileManager.getAllData().stream()
                            .filter(data -> data.getPremiumData().getStatus().isPremium())
                            .forEach(PersistentUserData::resetPasswords);
                    return true;
                }
                case "abstats":
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player player = (Player) sender;
                    if (ABStats.reversePresence(UserManager.getVerifiedUser(player))) {
                        sendMessage(sender, Messages.get("as-abstats-added"));
                        if (AlixAtaraxia.isEnabled())
                            sendMessage(sender, Messages.get("as-abstats-ataraxia-warning"));
                    } else sendMessage(sender, Messages.get("as-abstats-removed"));
                    break;
                //Unnecessary since pre-join thread disconnect
                /*case "connection-setup":
                case "c-s":
                    long val = 500;
                    ReflectionUtils.setConnectionThrottle(val);
                    sendMessage(sender, "Set the connection-throttle to " + val + ".");
                    break;*/
                case "m-e":
                case "messages-extract":
                    boolean success = Messages.extract();
                    if (!success)
                        sendMessage(sender, Messages.get("as-messages-extract-already-exists"));
                    else sendMessage(sender, Messages.get("as-messages-extract-success"));
                    break;
                case "m-m":
                case "messages-merge":
                    boolean succeeded = Messages.merge();
                    if (!succeeded) {
                        sendMessage(sender, Messages.get("as-messages-merge-not-extracted"));
                    } else
                        sendMessage(sender, Messages.get("as-messages-merge-success"));
                    break;
/*                case "gui":
                    if (isConsoleButPlayerRequired(sender)) break;
                    Inventory inv = Bukkit.createInventory(null, 54, "AlixSystem's DataBase");
                    byte i = 0;
                    for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                        PersistentUserData data = UserFileManager.get(p.getName());
                        if (data == null) continue;
                        UserDataFormatter f = JavaFormatter.formatPersistentData(data);
                        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) item.getItemMeta();
                        meta.setDisplayName(translateColors("&e" + data.getName() + "'s Data:"));
                        meta.setLore(getItemLore("&7Password Hash: &c" + f.getPasswordFormat(),
                                "&7IP: &c" + f.getIPFormat(), "&7Muted: &c" + f.getMutedFormat()));
                        meta.setOwningPlayer(p);
                        item.setItemMeta(meta);
                        inv.setItem(i++, item);
                        if (i == 54) break;
                    }
                    ((Player) sender).openInventory(inv);
                    break;*/
                case "incognitooff":
                    if (isConsoleButPlayerRequired(sender)) break;
                    Player p = (Player) sender;
                    JsonObject texture = parseTexture(p.getName());
                    if (texture == null) texture = parseTexture("Alex");
                    setName(p, null);
                    setSkin(p, texture);
                    sendMessage(sender, Messages.get("as-incognitooff-success"));
                    break;
                case "losowerownanie":
                case "randommath":
                case "rmath":
                    sendMessage(sender, getRandomMathematicalOperation());
                    break;
                case "cons":
                case "constants":
                    sendMessage(sender, "");
                    sendMessage(sender, Messages.get("as-constants-random", random.nextDouble()));
                    sendMessage(sender, Messages.get("as-constants-ipsylon"));
                    sendMessage(sender, Messages.get("as-constants-pi"));
                    sendMessage(sender, Messages.get("as-constants-f"));
                    sendMessage(sender, Messages.get("as-constants-e"));
                    sendMessage(sender, Messages.get("as-constants-k"));
                    sendMessage(sender, Messages.get("as-constants-alfa"));
                    sendMessage(sender, Messages.get("as-constants-omega"));
                    sendMessage(sender, "");
                    break;
                /*case "pings":
                    if (!ServerPingManager.isRegistered()) {
                        sendMessage(sender, "&cServerPingManager is disabled. Please set the parameter 'ping-before-join' in the config.yml " +
                                "file to true if you want to enable it.");
                        return false;
                    }
                    String[] pings = ServerPingManager.getUserReadablePings();
                    if (pings.length == 0) {
                        sendMessage(sender, "There are currently no pings.");
                        return true;
                    }
                    sendMessage(sender, "List of all current server pings: ");
                    sendMessage(sender, "");
                    for (String s : pings) sendMessage(sender, s);
                    sendMessage(sender, "");
                    break;*/
/*                    case "rl":
                    case "reload":
                        dispatch(sender, "AlixSystem is currently reloading..");
                        dispatchServerCommand("reload");
                        dispatchServerCommand("reload confirm");
                        JavaHandler.handleReload();
                        dispatch(sender, "Done!");
                        break;*/
                case "info":
                    long[] memory = getMemory();
                    float usage = getPercentOfMemoryUsage(memory);
                    sendMessage(sender, "");
                    sendMessage(sender, Messages.get("as-info-time", getTime(new Date())));
                    sendMessage(sender, Messages.get("as-info-memory-usage", getColorizationToMemoryUsage(usage), usage));
                    sendMessage(sender, Messages.get("as-info-memory-free", memory[0]));
                    sendMessage(sender, Messages.get("as-info-memory-max", memory[1]));
                    sendMessage(sender, Messages.get("as-info-memory-total", memory[2]));
                    sendMessage(sender, Messages.get("as-info-active-threads", Thread.activeCount()));
                    sendMessage(sender, "");
                    break;
                default:
                    sendMessage(sender, Messages.get("as-unknown-command"));
                    break;
            }
        }
        return true;
    }
}
