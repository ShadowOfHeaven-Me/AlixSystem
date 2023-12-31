package shadow.utils.holders.methods.impl;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.users.offline.UnverifiedUser;

final class PacketEventsUserKick implements UserKickMethod {

    @Override
    public void kickAsync(UnverifiedUser user, String message) {
        Channel channel = user.getPacketBlocker().getChannel();
        channel.close();
    }
}