package alix.common.utils.multiengine.ban;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.util.Date;

public final class BukkitBanList implements AbstractBanList {

    public static final BukkitBanList IP = new BukkitBanList(BanList.Type.IP);
    public static final BukkitBanList NAME = new BukkitBanList(BanList.Type.NAME);

    private final BanList list;

    private BukkitBanList(BanList.Type type) {
        this.list = Bukkit.getBanList(type);
    }

    @Override
    public void ban(String target, String reason, Date until, String sender) {
        this.list.addBan(target, reason, until, sender);
    }

    @Override
    public void unban(String target) {
        this.list.pardon(target);
    }

    @Override
    public boolean isBanned(String target) {
        return this.list.isBanned(target);
    }

    public BanEntry getEntry(String target) {
        return this.list.getBanEntry(target);
    }

    public static BukkitBanList get(boolean ip) {
        return ip ? IP : NAME;
    }
}