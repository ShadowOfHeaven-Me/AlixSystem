package shadow.systems.login.captcha.manager;

import alix.common.messages.Messages;
import alix.common.utils.other.annotation.OptimizationCandidate;
import io.netty.buffer.ByteBuf;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

public final class VirtualCountdown {//shows xp countdown and kicks out

    private static final ByteBuf
            captchaTimePassedKickPacket = OutDisconnectPacketConstructor.constAtPlay(Messages.get("captcha-time-passed")),
            registerTimePassedKickPacket = OutDisconnectPacketConstructor.constAtPlay(Messages.get("register-time-passed")),
            loginTimePassedKickPacket = OutDisconnectPacketConstructor.constAtPlay(Messages.get("login-time-passed")),
            //Mirrors LimboCountdown's own emailVerificationTimePassedKickPacket on Velocity.
            registerEmailVerificationTimePassedKickPacket = OutDisconnectPacketConstructor.constAtPlay(Messages.get("register-email-verification-time-passed"));

    private static final ByteBuf timeOutError = OutDisconnectPacketConstructor.constAtPlay("§cTimed Out No Packet [Alix]");
    //private final ChannelHandlerContext ctx;
    private final UnverifiedUser user;
    private ByteBuf[] packets;
    private int index;
    //Whether this countdown is currently running in "waiting on a require-email-in-register verification
    //code" mode (see restartAsEmailVerification()) - determines which kick packet tick() uses once the
    //countdown reaches zero, since that state isn't otherwise derivable from the user's captcha/registered
    //flags the way the other three cases are.
    private boolean emailVerificationMode;

    public VirtualCountdown(UnverifiedUser user) {
        boolean completedCaptcha = user.hasCompletedCaptcha();
        this.user = user;
        //this.ctx = user.getSilentContext();//used in order to optimize PacketProcessor's implementation checks
        this.index = completedCaptcha ? BufferedPackets.loginPacketArraySize : BufferedPackets.captchaPacketArraySize;
        this.packets = completedCaptcha ? BufferedPackets.loginOutExperiencePackets : BufferedPackets.captchaOutExperiencePackets;
    }

    //can be optimized by caching raw packets or moving it to be an action bar message
    @OptimizationCandidate
    public void tick() {
        //Main.logError("TICKKKKK");
        if (index != 0) this.user.writeAndFlushConstSilently(this.packets[--this.index]);
        else MethodProvider.kickAsync(user, emailVerificationMode ? registerEmailVerificationTimePassedKickPacket
                : user.hasCompletedCaptcha() ? user.isRegistered() ? loginTimePassedKickPacket : registerTimePassedKickPacket : captchaTimePassedKickPacket);
    }

    public void tickNoPacket() {
        if (--this.index == 0) MethodProvider.kickAsync(this.user, this.user.captchaInitialized() ? captchaTimePassedKickPacket : timeOutError);
    }

    public void restartAsLogin() {
        this.packets = BufferedPackets.loginOutExperiencePackets;
        this.index = BufferedPackets.loginPacketArraySize;
        this.emailVerificationMode = false;
    }

    //Switches from the general max-login-time countdown to the (typically longer) dedicated
    //email-verification one - mirrors LoginState#handleRegisterCommandWithEmail()'s own countdown swap on
    //Velocity. Receiving the email can easily take longer than max-login-time allows for the rest of the
    //register flow, and without this the player could get kicked (losing the pending password/email, since
    //the account isn't created yet) before the code even arrives, forcing them to restart registration and
    //wait for a new email every time. A no-op when 'email-verification-time' isn't configured (the caller
    //checks ConfigParams#hasEmailVerificationTime first), leaving the general countdown running unswapped.
    public void restartAsEmailVerification() {
        this.packets = BufferedPackets.emailVerificationOutExperiencePackets;
        this.index = BufferedPackets.emailVerificationPacketArraySize;
        this.emailVerificationMode = true;
    }

    public static void pregenerate() {
        BufferedPackets.init();
    }
}