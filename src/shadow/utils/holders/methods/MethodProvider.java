package shadow.utils.holders.methods;

import alix.common.utils.other.annotation.AlixIntrinsified;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import shadow.utils.users.offline.UnverifiedUser;

public final class MethodProvider {

    //private static final MethodProvider instance = new MethodProvider();
    //private static final UserKickMethod kickMethod = UserKickMethod.createImpl();

    public static void kickAsync(UnverifiedUser user, Object disconnectPacket) {
        kickAsync(user.getPacketBlocker().getChannel(), disconnectPacket);
    }

    @AlixIntrinsified
    public static void kickAsync(Channel channel, Object disconnectPacket) {
        channel.writeAndFlush(disconnectPacket).addListener(ChannelFutureListener.CLOSE);
    }
}