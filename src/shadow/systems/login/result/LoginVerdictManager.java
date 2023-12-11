package shadow.systems.login.result;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static shadow.utils.main.AlixUtils.playerIPAutoLogin;

public final class LoginVerdictManager {

    static final Map<String, LoginInfo> map = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static void addOffline(String name, String ip, PersistentUserData data) {
        map.put(name, createInfo(ip, data));
    }

    public static void addOnline(String name) {
        map.put(name, LoginInfo.PREMIUM_LOGIN);
    }

    private static LoginInfo createInfo(String ip, PersistentUserData data) {
        if (data == null)//not registered - no data
            return new LoginInfo(LoginVerdict.DISALLOWED_NO_DATA, ip, data);

        if (!data.getPassword().isSet())//not registered - password was reset
            return new LoginInfo(LoginVerdict.DISALLOWED_PASSWORD_RESET, ip, data);

        if (playerIPAutoLogin && data.getSavedIP().equals(ip))//ip auto login
            return new LoginInfo(LoginVerdict.LOGIN_IP_AUTO_LOGIN, ip, data);

        return new LoginInfo(LoginVerdict.DISALLOWED_LOGIN_REQUIRED, ip, data); //not logged in
    }

    public static LoginInfo remove(String name) {
        return map.remove(name);
    }

    public static LoginInfo getNullable(String name) {
        return map.get(name);
    }

    public static LoginInfo getExisting(Player p) {
        LoginInfo l = getNullable(p.getName());
        if (l == null) {
            p.kickPlayer("§cNo Login Verdict was found!");
            throw new RuntimeException("No Login Verdict was found for the player " + p.getName() + "! Report this as an error immediately!");
        }
        return l;
    }

    private LoginVerdictManager() {
    }
}