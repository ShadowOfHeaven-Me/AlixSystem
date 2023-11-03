package shadow.systems.login.captcha.subtypes;

import shadow.systems.login.captcha.Captcha;
import shadow.utils.users.offline.UnverifiedUser;

import static shadow.utils.main.AlixUtils.captchaTitleStayTime;
import static shadow.utils.main.AlixUtils.translateColors;

public final class SubtitleCaptcha extends Captcha {

    public SubtitleCaptcha() {

    }

    public SubtitleCaptcha(Captcha captcha) {
        super(captcha);
    }

    @Override
    public Captcha inject(UnverifiedUser user) {
        user.getPlayer().sendTitle("Captcha: ", "§c" + captcha, 0, captchaTitleStayTime, 0);
        return super.inject(user);
    }

    @Override
    public void uninject() {
        user.getPlayer().resetTitle();
        //player.sendTitle(null, null, 0, 0, 0);
        super.uninject();
    }
}