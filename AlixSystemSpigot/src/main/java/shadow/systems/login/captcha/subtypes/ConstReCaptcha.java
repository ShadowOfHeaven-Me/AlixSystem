package shadow.systems.login.captcha.subtypes;

import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.runnables.futures.AlixFuture;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;

public final class ConstReCaptcha extends Captcha {

    public static final AlixFuture<Captcha> BEDROCK_RECAPTCHA = AlixScheduler.singleAlixFuture(ConstReCaptcha::new);
    private final ByteBuf[] buffers;

    private ConstReCaptcha() {
        this.buffers = ImageRenderer.recaptcha(ReCaptcha.SPAWN_LOC, NettyUtils::constBuffer);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        for (ByteBuf buf : this.buffers) user.writeAndFlushConstSilently(buf);
        //user.sendDynamicMessageSilently("sent packets");
    }

    @Override
    protected boolean isReleased() {
        return false;
    }
}