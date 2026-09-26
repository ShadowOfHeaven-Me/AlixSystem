package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.misc.packet.constructors.OutEntityDestroyPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NameCaptcha extends Captcha {

    private final ByteBuf[] buffers;
    //Whether this captcha has been SHOWN to a player - used by Captcha#uninject() to decide whether it's
    //safe to recycle back into the pre-generation pool, never gets set back to false.
    private final AtomicBoolean shown = new AtomicBoolean();
    //FUNCTIONALITY (audit, 2026-09-24): separate from "shown" above - see SmoothCaptcha's matching comment
    //for the full explanation of the bug this fixes (same pattern, same fix).
    private final AtomicBoolean buffersFreed = new AtomicBoolean();

    public NameCaptcha() {
        BufferedImage image = CaptchaImageGenerator.generateCaptchaImageX256(captcha, maxRotation, false, false);
        this.buffers = ImageRenderer.nameCaptchaBuffers(image, 256);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        this.shown.set(true);
        //created to spread out the packet sending, even just a bit
        user.writeAndFlushWithThresholdSilently(this.buffers, Math.min(this.buffers.length / 5 + 1, 100));
        //Main.logInfo("SENT CAPTCHAAA " + buffers.length);
    }

    @Override
    protected boolean isReleased() {
        return this.shown.get();
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        int idStart = ImageRenderer.ENTITY_ID_START + 1;

        user.writeAndFlushSilently(OutEntityDestroyPacketConstructor.constructDynamic(idStart, this.buffers.length >> 1));
        this.release();
    }

    @Override
    public void release() {
        if (this.buffersFreed.compareAndSet(false, true))
            for (ByteBuf buf : buffers) buf.release();
    }
}