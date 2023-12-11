package alix.common.antibot.connection.filters;

import alix.common.CommonAlixMain;
import alix.common.antibot.connection.ConnectionFilter;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigParams;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GeoIPTracker implements ConnectionFilter {

    public static final GeoIPTracker instance = ConfigParams.maximumTotalAccounts > 0 ? new GeoIPTracker() : null;
    private static final boolean initialized = instance != null;
    private static final String maxAccountsReached = Messages.get("account-limit-reached", ConfigParams.maximumTotalAccounts);
    private final TempIPCounter tempIPCounter;
    private final Map<String, Integer> map;//existing accounts

    public GeoIPTracker() {
        this.map = new ConcurrentHashMap<>();
        this.tempIPCounter = new TempIPCounter();
        //Main.debug(JavaUtils.isPluginLanguageEnglish ? "GeoIpTracker is being initialized..." : "GeoIpTracker został zainitializowany!");
    }

/*    public static void initialize() {
        Main.debug(JavaUtils.isPluginLanguageEnglish ? "GeoIpTracker has been initialized!" : "GeoIpTracker został zainitializowany!");
    }*/

    @Override
    public String getReason() {
        return maxAccountsReached;
    }
/*        return isPluginLanguageEnglish ? "You've reached the maximum amount of accounts! (" + maximumTotalAccounts + ")"
                : "Osiągnąłeś limit kont! (" + maximumTotalAccounts + ")";*/

    @Override
    public boolean disallowJoin(String address, String name) {//counts both: existing accounts and unregistered players currently on the server with that ip
        //CommonAlixMain.logInfo(tempIPCounter.getAccountsOf(address) + " " + getAccountsOf(address));
        return getAccountsOf(address) + tempIPCounter.getAccountsOf(address) >= ConfigParams.maximumTotalAccounts;
    }

    private int getAccountsOf(String ip) {
        //for (AccountData data : list) if (data.matches(ip)) return data.getAccounts();
        Integer i = map.get(ip);
        return i != null ? i : 0;
    }

/*    public boolean disallowJoin(String name, String ip) {
        if (UserFileManager.hasName(name)) return false;
        return getAccountsOf(ip) >= JavaUtils.maximumTotalAccounts;
    }*/

    private static final class TempIPCounter {

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
    }

    private void updateMap(String ip) {
        map.compute(ip, (k, v) -> v == null ? 1 : v + 1);
        tempIPCounter.removeIP(ip);//an account was created
        //map.compute(
/*        for (int i = 0; i < list.size(); i++) {
            AccountData data = list.get(i);
            if (data.matches(ip)) {
                list.set(i, data.enlarge());
                return;
            }
        }
        list.add(new AccountData(ip));*/
/*        Integer i = map.get(ip);
        if (i != null) map.replace(ip, i, i + 1);
        else map.put(ip, 1);*/
    }

    public static void addTempIP(String ip) {//unregistered player joined the server
        if (initialized) instance.tempIPCounter.addIP(ip);
    }

    public static void removeTempIP(String ip) {//unregistered player left the server
        if (initialized) instance.tempIPCounter.removeIP(ip);
    }

    public static boolean disallowLogin(String ip) {
        return initialized && instance.getAccountsOf(ip) >= ConfigParams.maximumTotalAccounts;
    }

    public static int getAccounts(String ip) {
        return initialized ? instance.getAccountsOf(ip) : -1;
    }

    public static void update(String ip) {
        if (initialized) instance.updateMap(ip);
    }
}