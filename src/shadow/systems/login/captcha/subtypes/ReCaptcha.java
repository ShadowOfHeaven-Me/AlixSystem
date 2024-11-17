package shadow.systems.login.captcha.subtypes;

import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.systems.login.captcha.consumer.ReCaptchaConsumer;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.misc.packet.constructors.OutEntityDestroyPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.location.ConstLocation;

import java.awt.*;

public final class ReCaptcha extends Captcha {

    public static final ConstLocation SPAWN_LOC = AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(1, 0, -1.5).toConst();
    public static final ConstLocation HEAD_LOC = SPAWN_LOC.asModifiableCopy().add(0, 1.75, 0).toConst();
    private final ByteBuf[] buffers;

    public ReCaptcha() {
        this.buffers = ImageRenderer.recaptcha(SPAWN_LOC, NettyUtils::createBuffer);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        for (ByteBuf buf : this.buffers) user.writeSilently(buf);
        user.flush();
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        user.writeAndFlushDynamicSilently(ReCaptchaConsumer.particle(Color.GREEN, HEAD_LOC));
        user.writeAndFlushSilently(OutEntityDestroyPacketConstructor.constructSingleDynamic(ImageRenderer.ENTITY_ID_START + 1));
    }

    @Override
    protected boolean isReleased() {
        return true;
    }
}