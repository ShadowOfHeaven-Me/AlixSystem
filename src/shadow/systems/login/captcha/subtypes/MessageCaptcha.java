package shadow.systems.login.captcha.subtypes;

import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.users.offline.UnverifiedUser;

public final class MessageCaptcha extends Captcha {

    public MessageCaptcha() {
        super();
    }

/*    public MessageCaptcha(Captcha captcha) {
        super(captcha);
    }*/

    @Override
    protected Captcha inject(UnverifiedUser user) {
        user.writeAndFlushSilently(OutMessagePacketConstructor.construct("\n Captcha: §c" + captcha + "\n"));//unoptimized, but I don't care, this is a deprecated and pretty much never used visual captcha type
        return super.inject(user);
    }
}