package shadow.systems.login.captcha.manager;

import alix.common.utils.netty.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import shadow.systems.netty.AlixChannelInjector;
import shadow.utils.holders.packet.buffered.BufferedPackets;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.users.offline.UnverifiedUser;

public final class CountdownTask {//shows countdown and kicks out

    private final ChannelHandlerContext ctx;
    private Object[] packets;
    private int index;

    public CountdownTask(UnverifiedUser user, boolean loginCountdown) {
        this.ctx = user.getDuplexHandler().getSilentContext();//in order to optimize PacketProcessor's implementation checks
        this.index = loginCountdown ? BufferedPackets.loginPacketArraySize : BufferedPackets.captchaPacketArraySize;
        this.packets = loginCountdown ? BufferedPackets.loginOutExperiencePackets : BufferedPackets.captchaOutExperiencePackets;
    }

    //Returns: Whether the player should be kicked
    public boolean tick() {
        if (index == 0) return true;
        NettyUtils.writeAndFlush(this.ctx, this.packets[--this.index]);
        return false;
    }

    public void setToLogin() {
        this.packets = BufferedPackets.loginOutExperiencePackets;
        this.index = BufferedPackets.loginPacketArraySize;
    }

    public static void pregenerate() {
        BufferedPackets.init();
    }
}