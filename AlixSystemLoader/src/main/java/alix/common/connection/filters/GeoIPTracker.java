package alix.common.connection.filters;

import alix.common.antibot.firewall.ataraxia.AlixAtaraxia;
import alix.common.data.file.AllowListFileManager;
import alix.common.messages.Messages;
import alix.common.utils.collections.fastutil.InetAddressMap;
import alix.common.utils.config.ConfigParams;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GeoIPTracker implements ConnectionFilter {

    private static final boolean initialized = ConfigParams.maximumTotalAccounts > 0;
    public static final String maxAccountsReached = Messages.get("account-limit-reached", ConfigParams.maximumTotalAccounts);

    public static final InetAddressMap<Integer> EXISTING_ACCOUNTS = new InetAddressMap<>(1 << 11, 1 << 5);//2048, 32
    //Plain Integer + compute(), same eviction pattern as EXISTING_ACCOUNTS/removeIP() below - a
    //LongAdder-based version only ever decrements and never removes the map entry at 0, leaking a permanent
    //entry per distinct IP under a sustained distributed-IP attack.
    private static final Map<InetAddress, Integer> TEMPORARY_ACCOUNTS = new ConcurrentHashMap<>(1 << 8);

    public static boolean disallowJoin(InetAddress ip, String name) {//counts both: existing accounts and unregistered players currently on the server with that ip
        //CommonAlixMain.logInfo(tempIPCounter.getAccountsOf(address) + " " + getAccountsOf(address));
        return initialized && !AllowListFileManager.has(name) && getAllAccountsOf(ip) >= ConfigParams.maximumTotalAccounts;//invoked only if registered, so non-negatives to negative comparison won't occur
    }

    @Override
    public boolean disallowJoin(InetAddress ip, String strAddress, String name) {
        return disallowJoin(ip, name);
    }

    @Override
    public String getReason() {
        return maxAccountsReached;
    }

    public static boolean isMapped(InetAddress ip) {
        return EXISTING_ACCOUNTS.containsKey(ip);
    }

    public static boolean isv4Mapped(int val) {
        return EXISTING_ACCOUNTS.containsV4(val);
    }

    public static int existingAccounts(InetAddress ip) {
        return EXISTING_ACCOUNTS.getOrDefault(ip, 0);
    }

    public static int tempAccounts(InetAddress ip) {
        var c = TEMPORARY_ACCOUNTS.get(ip);
        return c != null ? c : 0;
    }

    public static int getAllAccountsOf(InetAddress ip) {
        return existingAccounts(ip) + tempAccounts(ip);
    }

    //unregistered user joining
    public static void addTemporary(InetAddress ip) {
        TEMPORARY_ACCOUNTS.merge(ip, 1, Integer::sum);
    }

    public static void removeTemporary(InetAddress ip) {
        TEMPORARY_ACCOUNTS.compute(ip, (k, v) -> v != null && v != 1 ? v - 1 : null);
    }

    //added on data loading from a file
    public static void addExisting(InetAddress ip, boolean updateAtaraxia) {
        boolean notYetMapped = 1 == EXISTING_ACCOUNTS.merge(ip, 1, (current, one) -> current + 1);
        //see FireWallManager#add0()'s matching comment: AlixAtaraxia.isEnabled() must be checked before any
        //AlixAtaraxia call, since calling through it with no real IPC companion connected throws instead of
        //no-oping.
        if (notYetMapped && updateAtaraxia && AlixAtaraxia.isEnabled())
            AlixAtaraxia.whitelist(ip);
        //map.compute(ip, (k, v) -> v == null ? 1 : v + 1);
    }

    //removed when data is removed per /as frd <user> or on ip updates
    public static void removeIP(InetAddress ip) {
        //compute()'s remapping function returns null for BOTH "this was the last account, remove the
        //mapping" AND "there was no mapping to begin with" - compute() can't tell those apart, so comparing
        //its return value against null (as this used to) misreports an IP that was never mapped as "just
        //removed", calling unwhitelist() on Ataraxia for an IP it never whitelisted. computeIfPresent()'s
        //remapping function, unlike compute()'s, is only invoked when a mapping already existed, so the flag
        //it sets can only fire on a genuine last-account removal.
        boolean[] wasLastAccount = {false};
        EXISTING_ACCOUNTS.computeIfPresent(ip, (k, v) -> {
            if (v == 1) {
                wasLastAccount[0] = true;
                return null;
            }
            return v - 1;
        });
        if (wasLastAccount[0] && AlixAtaraxia.isEnabled())
            AlixAtaraxia.unwhitelist(ip);
    }
}