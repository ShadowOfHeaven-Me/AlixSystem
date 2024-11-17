package shadow.systems.login.captcha.subtypes;

import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

public final class MessageCaptcha extends Captcha {

    private final ByteBuf packet = OutMessagePacketConstructor.constructDynamic("\n Captcha: §c" + captcha + "\n");

    public MessageCaptcha() {
        super();
    }

/*    public MessageCaptcha(Captcha captcha) {
        super(captcha);
    }*/

    @Override
    public void sendPackets(UnverifiedUser user) {
        user.writeAndFlushSilently(this.packet);
    }

    @Override
    protected boolean isReleased() {
        return false;
    }
}