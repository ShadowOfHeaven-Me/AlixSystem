package shadow.systems.login.reminder.message;

import io.netty.buffer.ByteBuf;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.UnsafeNettyUtils;
import shadow.utils.users.types.UnverifiedUser;

import static shadow.utils.main.AlixUtils.getVerificationReminderMessagePacket;

final class ActionBarVerificationMessage extends AbstractVerificationMessage {

    private volatile ByteBuf rawVerificationMessageBuffer;

    ActionBarVerificationMessage(UnverifiedUser user) {
        super(user);
    }

    @Override
    public void updateAfterCaptchaComplete() {
        this.setVerificationMessageBuffer(AlixUtils.unregisteredUserMessagePacket);
    }

    @Override
    public void updateMessage() {
        this.setVerificationMessageBuffer(getVerificationReminderMessagePacket(this.user.isRegistered(), this.user.hasAccount()));
    }

    @Override
    public void spoof() {
        ByteBuf buf = this.rawVerificationMessageBuffer;
        if (buf != null) this.user.writeAndFlushRaw(buf);
    }

    @Override
    public void clearEffects() {//no functionality necessary here
    }

    @Override
    public void destroy() {
        this.releaseVerificationMessageBuffer();
    }

    private void setVerificationMessageBuffer(ByteBuf verificationMessageBuffer) {
        this.releaseVerificationMessageBuffer();

        //long t0 = System.nanoTime();
        this.rawVerificationMessageBuffer = UnsafeNettyUtils.sendAndGetRaw(this.user.silentContext(), this.user.bufHarvester, NettyUtils::constBuffer, verificationMessageBuffer.duplicate());
        //long diff0 = System.nanoTime() - t0;
        //Main.logError("ALLOCATING MESSAGE TIME: " + diff0 / Math.pow(10, 6) + "ms");
    }

    private void releaseVerificationMessageBuffer() {
        if (this.rawVerificationMessageBuffer != null) {
            //Main.logError("BUFFER: " + this.rawVerificationMessageBuffer + " " + this.rawVerificationMessageBuffer.unwrap());
            this.rawVerificationMessageBuffer.unwrap().release();//it's unreleasable - unwrap
            this.rawVerificationMessageBuffer = null;
        }
    }
}