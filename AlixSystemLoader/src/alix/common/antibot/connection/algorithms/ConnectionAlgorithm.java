package alix.common.antibot.connection.algorithms;

import org.bukkit.BanList;
import org.bukkit.Bukkit;

public interface ConnectionAlgorithm {

    void onJoinAttempt(String name, String ip);

    void onThreadRepeat();

    BanList ipBanList = Bukkit.getBanList(BanList.Type.IP);

}