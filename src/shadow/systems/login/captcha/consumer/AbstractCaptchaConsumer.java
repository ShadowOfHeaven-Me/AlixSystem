package shadow.systems.login.captcha.consumer;

import shadow.utils.users.types.UnverifiedUser;

abstract class AbstractCaptchaConsumer implements CaptchaConsumer {

    final UnverifiedUser user;

    AbstractCaptchaConsumer(UnverifiedUser user) {
        this.user = user;
    }
}