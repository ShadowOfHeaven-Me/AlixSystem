package shadow.systems.login.captcha.manager;

import alix.common.messages.Messages;
import alix.common.utils.other.annotation.OptimizationCandidate;
import io.netty.buffer.ByteBuf;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

public final class VirtualCountdown {//shows xp countdown and kicks out

    private static final ByteBuf
            captchaTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("captcha-time-passed")),
            registerTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("register-time-passed")),
            loginTimePassedKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(Messages.get("login-time-passed"));
    //private final ChannelHandlerContext ctx;
    private final UnverifiedUser user;
    private ByteBuf[] packets;
    private int index;

    public VirtualCountdown(UnverifiedUser user) {
        boolean login = user.hasCompletedCaptcha();
        this.user = user;
        //this.ctx = user.getSilentContext();//used in order to optimize PacketProcessor's implementation checks
        this.index = login ? BufferedPackets.loginPacketArraySize : BufferedPackets.captchaPacketArraySize;
        this.packets = login ? BufferedPackets.loginOutExperiencePackets : BufferedPackets.captchaOutExperiencePackets;
    }

    //can be optimized via caching raw packets or moving it to be an action bar message
    @OptimizationCandidate
    public void tick() {
        //Main.logError("TICKKKKK");
        if (index != 0) this.user.writeAndFlushConstSilently(this.packets[--this.index]);
        else MethodProvider.kickAsync(user, user.hasCompletedCaptcha() ? user.isRegistered() ? loginTimePassedKickPacket : registerTimePassedKickPacket : captchaTimePassedKickPacket);
    }

    public void tickNoPacket() {
        this.index--;
    }

    public void restartAsLogin() {
        this.packets = BufferedPackets.loginOutExperiencePackets;
        this.index = BufferedPackets.loginPacketArraySize;
    }

    public static void pregenerate() {
        BufferedPackets.init();
    }
}