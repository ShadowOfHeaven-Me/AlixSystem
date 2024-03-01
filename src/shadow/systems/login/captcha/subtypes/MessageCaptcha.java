package shadow.systems.login.captcha.subtypes;

import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.users.offline.UnverifiedUser;

public final class MessageCaptcha extends Captcha {

    private final Object packet = OutMessagePacketConstructor.construct("\n Captcha: §c" + captcha + "\n");

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
}