package shadow.systems.commands;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.database.migrate.MigrateManager;
import alix.common.database.migrate.MigrateType;
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
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.ReflectionUtils;
import shadow.utils.users.UserManager;

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
                    case "migrate": {
                        MigrateType type;
                        try {
                            type = MigrateType.valueOf(arg2.toUpperCase());
                        } catch (Exception e) {
                            sendMessage(sender, "'" + arg2 + " is not a supported migration type!");
                            return false;
                        }
                        AlixScheduler.asyncBlocking(() -> MigrateManager.migrate(type));
                        break;
                    }
                    case "bl":
                    case "bypasslist":
                    case "bypasslimit": {
                        if (AllowListFileManager.has(arg2)) {
                            sendMessage(sender, "&cName '" + arg2 + " is already on the account limit bypass list!");
                            return true;
                        }
                        AllowListFileManager.add(arg2);
                        sendMessage(sender, "Added name '" + arg2 + " to the account limit bypass list!");
                        break;
                    }
                    case "bl-r":
                    case "bypasslist-remove":
                    case "bypasslimit-remove": {
                        if (AllowListFileManager.remove(arg2)) {
                            sendMessage(sender, "Removed name '" + arg2 + " from the account limit bypass list!");
                            return true;
                        }
                        sendMessage(sender, "&cName '" + arg2 + " is not on the account limit bypass list!");
                        break;
                    }

                    case "frd":
                    case "fullyremovedata": {
                        PersistentUserData data = UserFileManager.remove(arg2);

                        if (AllowListFileManager.remove(arg2)) {
                            sendMessage(sender, "Removed the name " + arg2 + " from the allow-list!");
                        }

                        //todo: Also remove from UserTokensFileManager?
                        if (data == null) {
                            sendMessage(sender, playerDataNotFound.format(arg2));
                            return false;
                        }

                        GeoIPTracker.removeIP(data.getSavedIP());
                        sendMessage(sender, "Fully removed the data of the account " + arg2 + "!");
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

                        sendMessage(sender, "The premium status of the player " + arg2 + " has been set to UNKNOWN.");
                        break;
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
                            String arg3 = args[2].toUpperCase();

                            LoginType type;

                            try {
                                type = LoginType.valueOf(arg3);
                            } catch (Exception e) {
                                sendMessage(sender, "Available login types: COMMAND, PIN & ANVIL, but instead got: " + arg3);
                                return false;
                            }

                            data.setLoginType(type);
                            sendMessage(sender, "Successfully reset the password of the player " + arg2
                                    + " and set his password type to " + type + "!");
                        } else sendMessage(sender, "Successfully reset the password of the player " + arg2 + ".");
                    }
                    break;
                    case "user": {
                        AlixScheduler.async(() -> {
                            OfflinePlayer offlinePlayer = getOfflinePlayer(arg2);
                            if (offlinePlayer == null) {
                                sendMessage(sender, "&cPlayer " + arg2 + " has never joined this server before!");
                                return;
                            }
                            PersistentUserData data = UserFileManager.get(offlinePlayer.getName());
                            if (data == null) {
                                sendMessage(sender, "&cPlayer " + arg2 + " is not in AlixSystem's User DataBase!");
                                return;
                            }
                            boolean dVer = data.getLoginParams().isDoubleVerificationEnabled();
                            sendMessage(sender, "");
                            sendMessage(sender, "User " + offlinePlayer.getName() + " has the following data:");
                            sendMessage(sender, "IP: &c" + data.getSavedIP().getHostAddress());
                            //sendMessage(sender, "Password" + (data.getPassword().isHashed() ? " (Hashed): " : ": ") + data.getHashedPassword());
                            sendMessage(sender, "IP AutoLogin: &c" + (data.getLoginParams().getIpAutoLogin() ? "&cEnabled" : "&cDisabled"));
                            sendMessage(sender, "Login Type: &c" + data.getLoginType());
                            sendMessage(sender, "Premium Status: &c" + data.getPremiumData().getStatus().readableName());
                            if (dVer)
                                sendMessage(sender, "Second Login Type: &c" + data.getLoginParams().getExtraLoginType());

                            sendMessage(sender, "Double password verification: " + (dVer ? "&aEnabled" : "&cDisabled"));
                            String authApp;
                            switch (data.getLoginParams().getAuthSettings()) {
                                case PASSWORD:
                                    authApp = "&cDisabled";
                                    break;
                                case AUTH_APP:
                                    authApp = "&aEnabled - Just Auth App";
                                    break;
                                case PASSWORD_AND_AUTH_APP:
                                    authApp = "&aEnabled - Auth App and " + (dVer ? "two " : "") + "in-game password" + (dVer ? "s" : "");
                                    break;
                                default:
                                    throw new AlixError("Invalid - " + data.getLoginParams().getAuthSettings());
                            }
                            sendMessage(sender, "TOTP Auth app: " + authApp);
                            sendMessage(sender, "Has TOTP app linked: " + (data.getLoginParams().hasProvenAuthAccess() ? "&aYep" : "&cNope"));
                            sendMessage(sender, "First joined: &c" + getFormattedDate(new Date(offlinePlayer.getFirstPlayed())));
                            sendMessage(sender, (offlinePlayer.isOnline() ? "Currently online from: &c" : "Last joined: &c") + getFormattedDate(new Date(offlinePlayer.getLastPlayed())));
                            sendMessage(sender, "");
                        });
                        return true;
                    }
                    case "valueof":
                        if (isNumber(arg2)) sendMessage(sender, setAsClearNumber(arg2));
                        else sendMessage(sender, "&cExpected a number!");
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
                        sendMessage(sender, "&c" + toCalculate + " = " + result);
                        break;
                    case "avg":
                    case "average":
                        String[] array = skipArray(args, 1);
                        String entirety = setAsOne(array);
                        String r = setAsClearNumber(eval(entirety) / array.length);
                        sendMessage(sender, "&c(" + entirety + ")/" + array.length + " = " + r);
                        break;
                    case "forceop": {
                        if (!(sender instanceof ConsoleCommandSender)) {
                            sendMessage(sender, "Only the console is allowed to execute this command!");
                            return false;
                        }
                        OfflinePlayer p = getOfflinePlayer(arg1);
                        if (p == null || p.getName() == null) {
                            sendMessage(sender, "&eWARNING: Player " + arg1 + " has never joined this server before!");
                            AlixHandler.handleOperatorSet(sender, arg1);
                            return false;
                        }
                        AlixHandler.handleOperatorSet(sender, p.getName());
                        break;
                    }
                    case "forcedeop": {
                        if (!(sender instanceof ConsoleCommandSender)) {
                            sendMessage(sender, "Only the console is allowed to execute this command!");
                            return false;
                        }
                        OfflinePlayer p = getOfflinePlayer(arg1);
                        if (p == null || p.getName() == null) {
                            sendMessage(sender, "&eWARNING: Player " + arg1 + " has never joined this server before!");
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
                        sendMessage(sender, "&cTry /as help!");
                        break;
                }
                return true;
            }
            switch (arg1) {
                case "helpmath":
                    sendMessage(sender, "");
                    sendMessage(sender, "&c/as calc/calculate <mathematical operation> &7- " +
                            "Returns what the given mathematical operation is equal to. " +
                            "Example: &c/as calc sqrt(3) * cos(pi) / (sin(e) - 2^2) returns 0.48257042764929925");
                    sendMessage(sender, "&c/as avg/average <numbers> &7- " +
                            "Returns what the given numbers average is equal to. " +
                            "Example: &c/as avg 4 + 67 - 9 + 14 returns 19.");
                    sendMessage(sender, "&c/as valueof <number> &7- Returns a more readable version of a given number.");
                    sendMessage(sender, "&c/as cons/constants &7- Shows all constants that can be used in mathematical operations, in this plugin.");
                    sendMessage(sender, "&c/as randommath/rmath &7- Gives you random, already solved mathematical operation. " +
                            "Example: &c" + AlixUtils.getRandomMathematicalOperation());
                    sendMessage(sender, "");
                    break;
                case "help"://fullyremovedata
                    sendMessage(sender, "");//bypasslimit-remove
                    sendMessage(sender, "&c/as user <player> &7- Returns information about the given player.");
                    sendMessage(sender, "&c/as info &7- Informs about time, memory, and number of currently active server threads.");
                    sendMessage(sender, "&c/as abstats &7- Enables or disables the ability to view the AntiBot Statistics.");
                    sendMessage(sender, "&c/as bl/bypasslimit <name> &7- Adds the specified name to the account limit bypass list." +
                            " Such accounts are not restricted by the account limiter, no matter the config 'max-total-accounts' parameter.");
                    sendMessage(sender, "&c/as bl-r/bypasslimit-remove <name> &7- Removes the specified name from the account limit bypass list.");
                    sendMessage(sender, "&c/as rp/resetpassword <player> &7- Resets the player's password.");
                    sendMessage(sender, "&c/as rp/resetpassword <player> <login type> &7- Resets the player's password and changes their login type. Available login types: COMMAND, PIN & ANVIL.");
                    sendMessage(sender, "&c/as rs/resetstatus <player> &7- Resets the player's premium status. Mainly aimed to forgive cracked players who used /premium");

                    sendMessage(sender, "&c/as frd/fullyremovedata <player> &7- Fully removes all account data. The data cannot be restored after this operation.");
                    if (isOperatorCommandRestricted) {
                        sendMessage(sender, "&c/as forceop <player> &7- In case of having trouble with /op you can forcefully op a player, " +
                                "by executing this command in console.");
                        sendMessage(sender, "&c/as forcedeop <player> &7- In case of having trouble with /deop you can forcefully deop a player, " +
                                "by executing this command in console.");
                    }
                    sendMessage(sender, "&c/as helpmath &7- Lists alix commands related to math.");
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
                case "abstats":
                    if (isConsoleButPlayerRequired(sender)) return false;
                    Player player = (Player) sender;
                    if (ABStats.reversePresence(UserManager.getVerifiedUser(player))) {
                        sendMessage(sender, "&aAdded to AntiBot Statistics view!");
                        if (FireWallManager.isOsFireWallInUse)
                            sendMessage(sender, "&e[WARNING] The actual CPS can be different, because the OS IpSet FireWall is in use, and does not count dropped connections. The current CPS will only show the attempted connections that were not dropped by the OS beforehand.");
                    } else sendMessage(sender, "&cRemoved from AntiBot Statistics view!");
                    break;
                case "connection-setup":
                case "c-s":
                    long val = 500;
                    ReflectionUtils.setConnectionThrottle(val);
                    sendMessage(sender, "Set the connection-throttle to " + val + ".");
                    break;
                case "m-e":
                case "messages-extract":
                    boolean success = Messages.extract();
                    if (!success)
                        sendMessage(sender, "The extraction file already exists! Delete it manually if you want to extract the messages again.");
                    else sendMessage(sender, "The messages have been successfully extracted into another file!");
                    break;
                case "m-m":
                case "messages-merge":
                    boolean succeeded = Messages.merge();
                    if (!succeeded) {
                        sendMessage(sender, "The extraction file already does not exists! Extract the messages first, before trying to merging them.");
                    } else
                        sendMessage(sender, "The messages have been successfully merged into one file! You need to restart your server, for this change to take effect");
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
                    sendMessage(sender, "&6Your identity is now back to original!");
                    break;
                case "losowerownanie":
                case "randommath":
                case "rmath":
                    sendMessage(sender, getRandomMathematicalOperation());
                    break;
                case "cons":
                case "constants":
                    sendMessage(sender, "");
                    sendMessage(sender, "&cRandom &7- Returns random number, for example: " + random.nextDouble());
                    sendMessage(sender, "&cIpsylon &7- 4.66920160902990");
                    sendMessage(sender, "&cPi &7- 3.141592653589793");
                    sendMessage(sender, "&cF &7- 2.807770242028519");
                    sendMessage(sender, "&cE &7- 2.718281828459045");
                    sendMessage(sender, "&cK &7- 2.584981759579253");
                    sendMessage(sender, "&cAlfa &7- 2.502907875095892");
                    sendMessage(sender, "&cOmega &7- 0.56714329040978");
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
                    sendMessage(sender, "Time: &c" + getTime(new Date()));
                    sendMessage(sender, "Percent Of Memory Usage: " + getColorizationToMemoryUsage(usage) + usage + "%");
                    sendMessage(sender, "Free Memory: &c" + memory[0] + "MB");
                    sendMessage(sender, "Max Memory: &c" + memory[1] + "MB");
                    sendMessage(sender, "Total Memory: &c" + memory[2] + "MB");
                    sendMessage(sender, "Currently Active Threads: &c" + Thread.activeCount());
                    sendMessage(sender, "");
                    break;
                default:
                    sendMessage(sender, "&cTry /as help!");
                    break;
            }
        }
        return true;
    }
}