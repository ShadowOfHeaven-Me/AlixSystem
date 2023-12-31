package shadow.utils.holders.methods.impl;

import alix.common.scheduler.AlixScheduler;
import shadow.utils.users.offline.UnverifiedUser;

final class BukkitUserKick implements UserKickMethod {

    @Override
    public void kickAsync(UnverifiedUser user, String message) {
        AlixScheduler.sync(() -> user.getPlayer().kickPlayer(message));
    }
}