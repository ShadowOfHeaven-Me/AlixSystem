package shadow.systems.login.captcha.manager;

import alix.common.messages.Messages;
import alix.common.utils.netty.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.buffered.BufferedPackets;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.users.offline.UnverifiedUser;

public final class CountdownTask {//shows countdown and kicks out

    private static final Object
            captchaTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(Messages.get("captcha-time-passed")),
            loginTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(Messages.get("login-time-passed"));
    private final ChannelHandlerContext ctx;
    private final UnverifiedUser user;
    private Object[] packets;
    private int index;

    public CountdownTask(UnverifiedUser user) {
        boolean login = user.hasCompletedCaptcha();
        this.user = user;
        this.ctx = user.getDuplexHandler().getSilentContext();//used in order to optimize PacketProcessor's implementation checks
        this.index = login ? BufferedPackets.loginPacketArraySize : BufferedPackets.captchaPacketArraySize;
        this.packets = login ? BufferedPackets.loginOutExperiencePackets : BufferedPackets.captchaOutExperiencePackets;
    }

    public void tick() {
        if (index != 0) {
            NettyUtils.writeAndFlush(this.ctx, this.packets[--this.index]);
            return;
        }
        MethodProvider.kickAsync(user, user.hasCompletedCaptcha() ? loginTimePassedKickPacket : captchaTimePassedKickPacket);
    }

    public void setToLogin() {
        this.packets = BufferedPackets.loginOutExperiencePackets;
        this.index = BufferedPackets.loginPacketArraySize;
    }

    public static void pregenerate() {
        BufferedPackets.init();
    }
}