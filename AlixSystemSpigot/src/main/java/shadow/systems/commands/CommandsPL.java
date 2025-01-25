/*
package shadow.systems.commands;

import com.google.gson.JsonObject;
import alix.common.antibot.algorithms.connection.types.ServerPingManager;
import shadow.utils.main.AlixHandler;
import alix.common.data.file.UserFileManager;
import alix.common.data.PersistentUserData;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Date;

import static shadow.utils.main.AlixUtils.*;

public class CommandsPL implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int l = args.length;
        if (l > 0) {
            String arg1 = args[0].toLowerCase();
            if (l > 1) {
                String arg2 = args[1];
                switch (arg1) {
*/
/*                    case "skin": {
                        if (isConsoleButPlayerRequired(sender)) break;
                        Player p = (Player) sender;
                        JsonObject texture = parseTexture(arg2);
                        if (texture == null) {
                            sendMessage(sender, "&cNie znaleziono tekstury!");
                            return false;
                        }
                        setSkin(p, texture);
                        sendMessage(sender, "&6Zmieniono skina!");
                        break;
                    }*//*

                    */
/*case "incognito": {
                        if (isConsoleButPlayerRequired(sender)) break;
                        Player p = (Player) sender;
                        JsonObject texture = parseTexture(arg2);
                        if (texture == null) {
                            sendMessage(sender, "&cNie znaleziono tekstury!");
                            return false;
                        }

                        setSkin(p, texture);
                        String nickname = "";
                        switch (l) {
                            case 2:
                                nickname = CustomNameHolder.generateRandomNickname();
                                break;
                            case 3:
                                nickname = CustomNameHolder.generateRandomNickname(checkBooleanString(args[2], p), random.nextBoolean());
                                break;
                            case 4:
                                nickname = CustomNameHolder.generateRandomNickname(checkBooleanString(args[2], p), checkBooleanString(args[3], p));
                                break;
                            default:
                                sendMessage(sender, "&cSpróbuj /as pomoc");
                                break;
                        }

                        setName(p, nickname);
                        sendMessage(sender, "&6Jesteś teraz: " + nickname);
                        break;
                    }*//*

                    case "gracz": {*/
/*
                        if (!isOmnipotent(sender)) {
                            sendMessage(sender, "&cError! Console or Owner permission level required! Try /as help!");
                            return false;
                        }*//*

                        OfflinePlayer offlinePlayer = getOfflinePlayer(arg2);
                        if (offlinePlayer == null) {
                            sendMessage(sender, "&cGracz " + arg2 + " nigdy nie dołączył na ten serwer!");
                            return false;
                        }
                        PersistentUserData data = UserFileManager.get(offlinePlayer.getName());
                        if (data == null) {
                            sendMessage(sender, "&cNie znaleziono żadnych informacji o graczu " + arg2 + " w Bazie Danych Użytkowników JavaSystem!");
                            return false;
                        }
                        //TODO: Ostatnio/Obecnie Online od
                        sendMessage(sender, "");
                        sendMessage(sender, "Gracz " + offlinePlayer.getName() + " posiada następujące informacje offline:");
                        sendMessage(sender, "IP: " + data.getSavedIP());
                        sendMessage(sender, "Hasło: " + data.getHashedPassword());
                        sendMessage(sender, "Po raz pierwszy dołączył: " + getFormattedDate(new Date(offlinePlayer.getFirstPlayed())));
                        sendMessage(sender, "Ostatnio online: " + getFormattedDate(new Date(offlinePlayer.getLastPlayed())));
                        sendMessage(sender, "");
                        return true;

                    }
                    case "wartosc":
                        if (isNumber(arg2)) sendMessage(sender, setAsClearNumber(arg2));
                        else sendMessage(sender, "&cSpróbuj /as pomoc!");
                        break;
                    case "obl":
                    case "oblicz":
                    case "policz":
                    case "calc":
                    case "calculate":
                        String toCalculate = setAsOne(skipArray(args, 1));
                        String replacedConstants = toCalculate.replaceAll("Random", String.valueOf(random.nextDouble())).
                                replaceAll("Omega", "0.56714329040978").replaceAll("Alfa", "2.502907875095892").
                                replaceAll("Ipsylon", "4.66920160902990").replaceAll("pi", "3.141592653589793").
                                replaceAll("E", "2.718281828459045").replaceAll("K", "2.584981759579253").
                                replaceAll("F", "2.807770242028519");
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
                    case "srednia":
                        String[] array = skipArray(args, 1);
                        String entirety = setAsOne(array);
                        String r = setAsClearNumber(eval(entirety) / array.length);
                        sendMessage(sender, "&c(" + entirety + ")/" + array.length + " = " + r);
                        break;
                    case "forceop": {
                        if (!(sender instanceof ConsoleCommandSender)) {
                            sendMessage(sender, "Jedynie konsola może wykonywać tą komendę!");
                            return false;
                        }
                        OfflinePlayer p = getOfflinePlayer(arg1);
                        if (p == null || p.getName() == null) {
                            sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy wcześniej nie wszedł na ten serwer!");
                            AlixHandler.handleOperatorSetPL(sender, arg1);
                            return false;
                        }
                        AlixHandler.handleOperatorSetPL(sender, p.getName());
                        break;
                    }
                    case "forcedeop": {
                        if (!(sender instanceof ConsoleCommandSender)) {
                            sendMessage(sender, "Jedynie konsola może wykonywać tą komendę!");
                            return false;
                        }
                        OfflinePlayer p = getOfflinePlayer(arg1);
                        if (p == null || p.getName() == null) {
                            sendMessage(sender, "&eUWAGA: Gracz " + arg1 + " nigdy wcześniej nie wszedł na ten serwer!");
                            AlixHandler.handleOperatorUnsetPL(sender, arg1);
                            return false;
                        }
                        AlixHandler.handleOperatorUnsetPL(sender, p.getName());
                        break;
                    }
                    case "item":
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
                        }
                    default:
                        sendMessage(sender, "&cSpróbuj /as pomoc!");
                        break;
                }
                return true;
            }
            switch (arg1) {
                case "pomoc":
                    sendMessage(sender, "");
                    sendMessage(sender, "&c/as gracz <gracz> &7- Zwraca informacje o danym graczu na serwerze.");
                    sendMessage(sender, "&c/as wartosc <liczba> &7- Zwraca bardziej czytelną liczbę.");
                    //dispatch(sender, "&c/as rl/przeladuj &7- Przeładowywuje plugin.");
                    sendMessage(sender, "&c/as stale/stalematematyczne &7- Zwraca informacje o wszystkich stałych matematycznych, jakie obsługuje ten plugin.");
                    sendMessage(sender, "&c/as info &7- Zwraca informacje na temat obecnego czasu, wolnej pamięci i aktywnych wątków serwerowych.");
                    sendMessage(sender, "&c/as obl/oblicz/policz <działanie matematyczne> &7- " +
                            "Rozwiązuje dane działanie matematyczne. " +
                            "Dla przykładu: &c/as obl sqrt(3) * cos(pi) / (sin(e) - 2^2) zwraca 0.48257042764929925");
*/
/*                    sendMessage(sender, "&c/as incognito <skin> &7- Zmienia twój skin, na skin osoby wcześniej przez ciebie zdefiniowanej oraz daje ci losowy nickname.");
                    sendMessage(sender, "&c/as incognito <skin> <true/false> &7- Zmienia twój skin, na skin osoby wcześniej przez ciebie zdefiniowanej," +
                            " oraz zmienia twój losowy nickname, jeżeli otrzymano 'true' na angielski, a jeżeli 'false' na polski.");
                    sendMessage(sender, "&c/as incognito <skin> <true/false> <true/false> &7- Zmienia twój skin, na skin osoby wcześniej przez ciebie zdefiniowanej," +
                            "oraz zmienia twój losowy nickname, jeżeli otrzymano w pierwszym argumancie 'true' na angielski, a jeżeli 'false', na polski, " +
                            "oraz jeżeli w drugim argumencie otrzymano 'true' losowy nickname będzie zawierał w sobie faktyczne imię, a jeżeli 'false'" +
                            " to będzie on zawierał nazwę rzeczy.");*//*

                    sendMessage(sender, "&c/as pings &7- Zwraca listę wszystkich przechowywanych adresów ip które spingowały ten serwer.");
                    sendMessage(sender, "&c/as incognitooff &7- Zwraca ci twój oryginalny nickname");// i skin.");
                    sendMessage(sender, "&c/as randommath/rmath &7- Zwraca losowe działanie matematyczne" +
                            "Dla przykładu: &c" + getRandomMathematicalOperation());
                    sendMessage(sender, "&c/as forceop <gracz> &7- W razie problemów z komendą /op, możesz forsownie ustawić graczowi operatora, " +
                            "poprzez wykonanie tej komendy przez konsolę.");
                    sendMessage(sender, "&c/as forcedeop <gracz> &7- W razie problemów z komendą /deop, możesz forsownie zabrać graczowi operatora, " +
                            "poprzez wykonanie tej komendy przez konsolę.");
                    sendMessage(sender, "");
                    break;
                case "incognitooff":
                    if (isConsoleButPlayerRequired(sender)) break;
                    Player p = (Player) sender;
                    JsonObject texture = parseTexture(p.getName());
                    if (texture == null) texture = parseTexture("Alex");
                    setName(p, null);
                    setSkin(p, texture);
                    sendMessage(sender, "&6Your identity is now back to original!");
                    break;
                case "randommath":
                case "rmath":
                    sendMessage(sender, getRandomMathematicalOperation());
                    break;
                case "stale":
                case "stalematematyczne":
                    sendMessage(sender, "");
                    sendMessage(sender, "&cRandom &7- Zwraca losową liczbę, dla przykładu: " + random.nextDouble());
                    sendMessage(sender, "&cIpsylon &7- 4.66920160902990");
                    sendMessage(sender, "&cPi &7- 3.141592653589793");
                    sendMessage(sender, "&cF &7- 2.807770242028519");
                    sendMessage(sender, "&cE &7- 2.718281828459045");
                    sendMessage(sender, "&cK &7- 2.584981759579253");
                    sendMessage(sender, "&cAlfa &7- 2.502907875095892");
                    sendMessage(sender, "&cOmega &7- 0.56714329040978");
                    sendMessage(sender, "");
                    break;
*/
/*                    case "rl":
                    case "przeladuj":
                        dispatch(sender, "JavaSystem się obecnie przeładowuje..");
                        dispatchServerCommand("reload");
                        dispatchServerCommand("reload confirm");
                        JavaHandler.handleReload();
                        dispatch(sender, "Zakończono!");
                        break;*//*

                case "pings":
                    if (!ServerPingManager.isRegistered()) {
                        sendMessage(sender, "&cServerPingManager jest wyłączony. Ustaw parametr 'ping-before-join' w pliku config.yml " +
                                "na 'true' jeżeli chcesz go włączyć.");
                        return false;
                    }
                    String[] pings = ServerPingManager.getUserReadablePings();
                    if (pings.length == 0) {
                        sendMessage(sender, "Obecnie nie ma żadnych pingów.");
                        return true;
                    }
                    sendMessage(sender, "Lista wszystkich pingów: ");
                    sendMessage(sender, "");
                    for (String s : pings) sendMessage(sender, s);
                    sendMessage(sender, "");
                    break;
                case "info":
                    long[] memory = getMemory();
                    float usage = getPercentOfMemoryUsage(memory);
                    sendMessage(sender, "");
                    sendMessage(sender, "Czas: &c" + getTime(new Date()));
                    sendMessage(sender, "Procent Zużycia Pamięci: " + getColorizationToMemoryUsage(usage) + usage + "%");
                    sendMessage(sender, "Wolna Pamięć: &c" + memory[0] + "MB");
                    sendMessage(sender, "Maksymalna Pamięć: &c" + memory[1] + "MB");
                    sendMessage(sender, "Całkowita Pamięć: &c" + memory[2] + "MB");
                    sendMessage(sender, "Obecnie Aktywnych Wątków: &c" + Thread.activeCount());
                    sendMessage(sender, "");
                    break;
                default:
                    sendMessage(sender, "&cSpróbuj /as pomoc!");
                    break;
            }
        }
        return false;
    }
}
*/
