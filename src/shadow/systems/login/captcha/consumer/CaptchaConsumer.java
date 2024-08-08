package shadow.systems.login.captcha.consumer;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.world.Location;
import shadow.utils.users.types.UnverifiedUser;

public interface CaptchaConsumer {

    void onMove(PacketPlayReceiveEvent event);

    void onClick();

    Location getLocation();

    static CaptchaConsumer adequateFor(UnverifiedUser user) {
        return user.hasCompletedCaptcha() ? null : new Model3dCaptchaConsumer(user);
    }
}