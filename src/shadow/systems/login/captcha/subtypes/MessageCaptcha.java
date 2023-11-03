package shadow.systems.login.captcha.subtypes;

import shadow.systems.login.captcha.Captcha;
import shadow.utils.users.offline.UnverifiedUser;

public final class MessageCaptcha extends Captcha {

    public MessageCaptcha() {
        super();
    }

    public MessageCaptcha(Captcha captcha) {
        super(captcha);
    }

    @Override
    public Captcha inject(UnverifiedUser user) {
        user.getPlayer().sendRawMessage("\n Captcha: §c" + captcha + "\n");
        return super.inject(user);
    }
}