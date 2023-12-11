package shadow.utils.holders.methods.impl;

import alix.common.scheduler.impl.AlixScheduler;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.users.offline.UnverifiedUser;

final class BukkitUserKick implements UserKickMethod {

    @Override
    public void kickPlayerAsyncInvoked(UnverifiedUser user, String message) {
        AlixScheduler.sync(() -> user.getPlayer().kickPlayer(message));
    }
}