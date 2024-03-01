package shadow.systems.login.captcha.subtypes;

import shadow.systems.login.captcha.Captcha;
import shadow.utils.users.offline.UnverifiedUser;

import static shadow.utils.main.AlixUtils.maxCaptchaTime;

public final class SubtitleCaptcha extends Captcha {

    public SubtitleCaptcha() {
        super();
    }

/*    public SubtitleCaptcha(Captcha captcha) {
        super(captcha);
    }*/

    @Override
    public void sendPackets(UnverifiedUser user) {
        user.getPlayer().sendTitle("Captcha: ", "§c" + captcha, 0, maxCaptchaTime * 20, 0);
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        user.getPlayer().resetTitle();
        super.onCompletion(user);
    }
}