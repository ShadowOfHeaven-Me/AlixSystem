package shadow.utils.holders.methods.impl;

import org.bukkit.entity.Player;
import shadow.systems.dependencies.Dependencies;
import shadow.utils.users.offline.UnverifiedUser;

public interface UserKickMethod {

    void kickPlayerAsyncInvoked(UnverifiedUser user, String message);

    static UserKickMethod createImpl() {
        return Dependencies.isPacketEventsPresent ? new PacketEventsUserKick() : new BukkitUserKick();
    }
}