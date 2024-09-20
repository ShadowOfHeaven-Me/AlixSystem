package shadow.systems.login.captcha.subtypes;

import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.location.ConstLocation;

public final class ReCaptcha extends Captcha {

    public static final ConstLocation SPAWN_LOC = AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(1, 0, -1.5).toConst();
    public static final ConstLocation HEAD_LOC = SPAWN_LOC.asModifiableCopy().add(0, 1.75, 0).toConst();
    private final ByteBuf[] buffers;

    public ReCaptcha() {
        this.buffers = ImageRenderer.recaptcha(SPAWN_LOC, NettyUtils::createBuffer);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        for (ByteBuf buf : this.buffers) user.writeAndFlushSilently(buf);
    }
}