package alix.common.antibot.connection.algorithms;

public interface ConnectionAlgorithm {

    void onJoinAttempt(String name, String ip);

    void onThreadRepeat();

    String getAlgorithmID();

    //BanList ipBanList = Bukkit.getBanList(BanList.Type.IP);

}