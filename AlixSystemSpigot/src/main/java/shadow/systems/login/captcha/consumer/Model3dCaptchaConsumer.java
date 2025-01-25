/*
package shadow.systems.login.captcha.consumer;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.subtypes.ModelCaptcha;
import shadow.utils.users.types.UnverifiedUser;

final class Model3dCaptchaConsumer extends AbstractCaptchaConsumer {

    private Location loc;

    Model3dCaptchaConsumer(UnverifiedUser user) {
        super(user);
    }

    public void onMove(PacketPlayReceiveEvent event) {
        WrapperPlayClientPlayerFlying move = new WrapperPlayClientPlayerFlying(event);
        this.loc = move.getLocation();
        //Main.logInfo("MOVE: " + this.loc);
    }

    @Override
    public void onClick() {
        Captcha captcha = this.user.getCaptchaFuture().value();
        if (captcha != null) ((ModelCaptcha) captcha).onClick(this.user, this.loc);
    }

    public Location getLocation() {
        return loc;
    }
}*/
