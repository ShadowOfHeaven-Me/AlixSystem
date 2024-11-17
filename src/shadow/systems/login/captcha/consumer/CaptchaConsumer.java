package shadow.systems.login.captcha.consumer;

import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import shadow.utils.users.types.UnverifiedUser;

public interface CaptchaConsumer {

    void onMove(PacketPlayReceiveEvent event);

    void onClick();

    //Location getLocation();

    static CaptchaConsumer adequateFor(UnverifiedUser user) {
        return AbstractCaptchaConsumer.createFor0(user);
    }
}