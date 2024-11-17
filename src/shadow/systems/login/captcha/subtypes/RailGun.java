package shadow.systems.login.captcha.subtypes;

import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.users.types.UnverifiedUser;

public final class RailGun extends Captcha {

    private final ByteBuf[] bufs;

    public RailGun() {
        this.bufs = ImageRenderer.railGun();
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        user.writeAndOccasionallyFlushSilently(this.bufs);
    }

    @Override
    protected boolean isReleased() {
        return false;
    }
}