package shadow.systems.login.captcha.manager;

import alix.common.messages.Messages;
import io.netty.buffer.ByteBuf;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.buffered.BufferedPackets;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

public final class CountdownTask {//shows xp countdown and kicks out

    private static final ByteBuf
            captchaTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("captcha-time-passed")),
            registerTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("register-time-passed")),
            loginTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("login-time-passed"));
    //private final ChannelHandlerContext ctx;
    private final UnverifiedUser user;
    private ByteBuf[] packets;
    private int index;

    public CountdownTask(UnverifiedUser user) {
        boolean login = user.hasCompletedCaptcha();
        this.user = user;
        //this.ctx = user.getSilentContext();//used in order to optimize PacketProcessor's implementation checks
        this.index = login ? BufferedPackets.loginPacketArraySize : BufferedPackets.captchaPacketArraySize;
        this.packets = login ? BufferedPackets.loginOutExperiencePackets : BufferedPackets.captchaOutExperiencePackets;
    }

    public void tick() {
        if (index != 0) this.user.writeAndFlushConstSilently(this.packets[--this.index]);
        else MethodProvider.kickAsync(user, user.hasCompletedCaptcha() ? user.isRegistered() ? loginTimePassedKickPacket : registerTimePassedKickPacket : captchaTimePassedKickPacket);
    }

    public void restartAsLogin() {
        this.packets = BufferedPackets.loginOutExperiencePackets;
        this.index = BufferedPackets.loginPacketArraySize;
    }

    public static void pregenerate() {
        BufferedPackets.init();
    }
}