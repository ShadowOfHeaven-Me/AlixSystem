package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.misc.packet.constructors.OutEntityDestroyPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SmoothCaptcha extends Captcha {

    private final ByteBuf[] buffers;
    private final AtomicBoolean released = new AtomicBoolean();

    public SmoothCaptcha() {
        BufferedImage image = CaptchaImageGenerator.generateCaptchaImageX256(captcha, maxRotation, false, false);
        this.buffers = ImageRenderer.smoothModelBuffers(image);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        this.released.set(true);
        //created to spread out the packet sending, even just a bit
        user.writeAndFlushWithThresholdSilently(this.buffers, Math.max(this.buffers.length / 5 + 1, 100));
        //Main.logInfo("SENT CAPTCHAAA " + buffers.length);
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        int idStart = ImageRenderer.ENTITY_ID_START + 1;

        user.writeAndFlushSilently(OutEntityDestroyPacketConstructor.constructDynamic(idStart, this.buffers.length / 3));
    }

    @Override
    public void release() {
        if (this.released.compareAndSet(false, true))
            for (ByteBuf buf : buffers) buf.release();
    }

    @Override
    protected boolean isReleased() {
        return this.released.get();
    }
}