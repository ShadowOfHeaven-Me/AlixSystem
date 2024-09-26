package alix.common.connection.filters;

import alix.common.data.file.AllowListFileManager;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigParams;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GeoIPTracker implements ConnectionFilter {

    public static final GeoIPTracker INSTANCE = ConfigParams.maximumTotalAccounts > 0 ? new GeoIPTracker() : null;
    private static final boolean initialized = INSTANCE != null;
    public static final String maxAccountsReached = Messages.get("account-limit-reached", ConfigParams.maximumTotalAccounts);
    //private final TempIPCounter tempIPCounter;

    private final Map<InetAddress, Integer> map;//existing accounts

    public GeoIPTracker() {
        this.map = new ConcurrentHashMap<>(1 << 8);//256
        //Main.debug(JavaUtils.isPluginLanguageEnglish ? "GeoIpTracker is being initialized..." : "GeoIpTracker został zainitializowany!");
    }

/*    public static void initialize() {
        Main.debug(JavaUtils.isPluginLanguageEnglish ? "GeoIpTracker has been initialized!" : "GeoIpTracker został zainitializowany!");
    }*/
/*        return isPluginLanguageEnglish ? "You've reached the maximum amount of accounts! (" + maximumTotalAccounts + ")"
                : "Osiągnąłeś limit kont! (" + maximumTotalAccounts + ")";*/


    public static boolean disallowJoin(InetAddress ip, String name) {//counts both: existing accounts and unregistered players currently on the server with that ip
        //CommonAlixMain.logInfo(tempIPCounter.getAccountsOf(address) + " " + getAccountsOf(address));
        return initialized && !AllowListFileManager.has(name) && INSTANCE.getAccountsOf(ip) >= ConfigParams.maximumTotalAccounts;//invoked only if registered, so non-negatives to negative comparison won't occur
    }

    @Override
    public boolean disallowJoin(InetAddress ip, String strAddress, String name) {
        return disallowJoin(ip, name);
    }

    @Override
    public String getReason() {
        return maxAccountsReached;
    }

    private int getAccountsOf(InetAddress ip) {
        return this.map.getOrDefault(ip, 0);
    }

/*    public boolean disallowJoin(String name, String ip) {
        if (UserFileManager.hasName(name)) return false;
        return getAccountsOf(ip) >= JavaUtils.maximumTotalAccounts;
    }*/

    /*private static final class TempIPCounter {

        private final Map<String, Integer> map;//not registered user ips currently on the server

        private TempIPCounter() {
            this.map = new HashMap<>();
        }

        private int getAccountsOf(String ip) {
            Integer i = map.get(ip);
            return i != null ? i : 0;
        }

        private void addIP(String ip) {
            map.compute(ip, (k, v) -> v == null ? 1 : v + 1);
        }

        private void removeIP(String ip) {
            map.compute(ip, (k, v) -> v != null && v != 1 ? v - 1 : null);
        }
    }*/

    private void add0(InetAddress ip) {
        this.map.merge(ip, 1, (current, one) -> current + 1);
        //map.compute(ip, (k, v) -> v == null ? 1 : v + 1);
        //tempIPCounter.removeIP(ip);//an account was created
    }

    private void remove0(InetAddress ip) {
        this.map.compute(ip, (k, v) -> v != null && v != 1 ? v - 1 : null);
        //tempIPCounter.removeIP(ip);//an temporary ip quit
    }

    //added either on data loading from a file or unregistered user joining
    public static void addIP(InetAddress ip) {
        if (initialized) INSTANCE.add0(ip);
    }

    //removed only on quit of unregistered users
    public static void removeIP(InetAddress ip) {
        if (initialized) INSTANCE.remove0(ip);
    }
}