package shadow.systems.login.reminder.message;

import alix.common.messages.Messages;
import alix.libs.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerClearTitles;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.misc.packet.constructors.OutTitlePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.UnsafeNettyUtils;
import shadow.utils.users.types.UnverifiedUser;

import static shadow.utils.main.AlixUtils.maxLoginTime;

final class TitleVerificationMessage extends AbstractVerificationMessage {

    private static final ByteBuf emptyActionBar = OutMessagePacketConstructor.constructConst(Component.empty(), true);
    private static final ByteBuf[] resetTitleConstBuffers =
            NettyUtils.exists(PacketType.Play.Server.CLEAR_TITLES) ?
            new ByteBuf[]{NettyUtils.constBuffer(new WrapperPlayServerClearTitles(true))} :
            OutTitlePacketConstructor.constructConst("","", 0, 0, 0);
    private static final ByteBuf[]
            loginTitleConstBuffer = OutTitlePacketConstructor.constructConst(Messages.get("reminder-login-title"), Messages.get("reminder-login-subtitle"), 0, maxLoginTime * 60, 0),
            registerTitleConstBuffer = OutTitlePacketConstructor.constructConst(Messages.get("reminder-register-title"), AlixUtils.requirePasswordRepeatInRegister ? Messages.get("reminder-register-subtitle-repeat") : Messages.get("reminder-register-subtitle"), 0, maxLoginTime * 60, 0);
    private volatile ByteBuf rawCaptchaMsgBuffer;

    TitleVerificationMessage(UnverifiedUser user) {
        super(user);
    }

    private void initCaptchaMsgBuffer(ByteBuf captchaMsgBuffer) {
        this.releaseCaptchaMsgBuffer();

        this.rawCaptchaMsgBuffer = UnsafeNettyUtils.sendAndGetRaw(this.user.silentContext(), this.user.bufHarvester, NettyUtils::constBuffer, captchaMsgBuffer.duplicate());
    }

    private void releaseCaptchaMsgBuffer() {
        if (this.rawCaptchaMsgBuffer != null) {
            this.rawCaptchaMsgBuffer.unwrap().release();//it's unreleasable - unwrap
            this.rawCaptchaMsgBuffer = null;
        }
    }

    @Override
    public void updateAfterCaptchaComplete() {
        this.releaseCaptchaMsgBuffer();
        this.update();
    }

    @Override
    public void updateMessage() {
        this.update();
    }

    private void update() {
        this.clearEffects();
        this.releaseCaptchaMsgBuffer();
        if (this.user.isGUIUser()) return;

        if (!this.user.hasCompletedCaptcha()) {
            this.initCaptchaMsgBuffer(AlixUtils.captchaNotCompletedUserMessagePacket);
            return;
        }
        if (this.user.captchaInitialized()) this.user.writeConstSilently(emptyActionBar);

        ByteBuf[] bufs = this.user.isRegistered() ? loginTitleConstBuffer : registerTitleConstBuffer;

        this.user.writeAllConstAndThenFlushSilently(bufs);
    }

    @Override
    public void spoof() {//invoked only for captcha action bar reminders
        ByteBuf buf = this.rawCaptchaMsgBuffer;
        if (buf != null) this.user.writeAndFlushRaw(buf);
        //throw new AlixError("spoof() invoked on TitleVerificationMessage");
    }

    @Override
    public void clearEffects() {
        this.user.writeAllConstAndThenFlushSilently(resetTitleConstBuffers);
    }

    @Override
    public void destroy() {
        this.releaseCaptchaMsgBuffer();
    }
}