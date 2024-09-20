package shadow.systems.login.captcha.consumer;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.util.Vector3f;
import shadow.utils.users.types.UnverifiedUser;

public interface CaptchaConsumer {

    void onMove(PacketPlayReceiveEvent event);

    void onClick(Vector3f clickVector);

    //Location getLocation();

    static CaptchaConsumer adequateFor(UnverifiedUser user) {
        return AbstractCaptchaConsumer.createFor0(user);
    }
}