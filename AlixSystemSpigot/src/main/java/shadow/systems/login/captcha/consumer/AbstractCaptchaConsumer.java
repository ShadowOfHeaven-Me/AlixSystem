package shadow.systems.login.captcha.consumer;


import shadow.systems.login.captcha.Captcha;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.types.UnverifiedUser;

import java.util.function.Function;

abstract class AbstractCaptchaConsumer<T extends Captcha> implements CaptchaConsumer {

    final UnverifiedUser user;

    AbstractCaptchaConsumer(UnverifiedUser user) {
        this.user = user;
    }

    T captcha() {
        return (T) user.getCaptchaFuture().value();
    }

    static CaptchaConsumer createFor0(UnverifiedUser user) {
        if (user.hasCompletedCaptcha()) return null;
        //if (user.isBedrock()) return new BedrockReCaptchaConsumer(user);
        return Holder.func.apply(user);
    }

    private static final class Holder {

        private static final Function<UnverifiedUser, CaptchaConsumer> func = createFunc0();

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        private static Function<UnverifiedUser, CaptchaConsumer> createFunc0() {
            switch (AlixUtils.captchaVerificationVisualType) {
                case RECAPTCHA:
                    return ReCaptchaConsumer::new;
                default:
                    return u -> null;
            }
        }
    }
}