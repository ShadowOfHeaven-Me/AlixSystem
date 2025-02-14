package shadow.systems.login.reminder.message;

import io.netty.buffer.ByteBuf;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.packets.AlixPacket;
import shadow.utils.users.types.UnverifiedUser;

import static shadow.utils.main.AlixUtils.getVerificationReminderMessagePacket;

final class ActionBarVerificationMessage extends AbstractVerificationMessage {

    private volatile AlixPacket rawVerificationMessage;
    //private volatile ByteBuf rawVerificationMessageBuffer;

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
        AlixPacket packet = this.rawVerificationMessage;
        if (packet != null) packet.writeAndFlush(this.user);
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

        this.rawVerificationMessage = AlixPacket.const0(verificationMessageBuffer);
        //long diff0 = System.nanoTime() - t0;
        //Main.logError("ALLOCATING MESSAGE TIME: " + diff0 / Math.pow(10, 6) + "ms");
    }

    private void releaseVerificationMessageBuffer() {
        /*AlixPacket packet = this.rawVerificationMessage;
        if (packet != null) {
            packet.release0();
            this.rawVerificationMessage = null;
        }*/
    }
}