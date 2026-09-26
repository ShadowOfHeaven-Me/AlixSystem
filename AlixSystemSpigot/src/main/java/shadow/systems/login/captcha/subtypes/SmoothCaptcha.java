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
    //Whether this captcha has been SHOWN to a player - used by Captcha#uninject() to decide whether it's
    //safe to recycle back into the pre-generation pool, never gets set back to false.
    private final AtomicBoolean shown = new AtomicBoolean();
    //FUNCTIONALITY (audit, 2026-09-24): separate from "shown" above - was the SAME AtomicBoolean, set true
    //as sendPackets()'s very first line, which permanently failed release()'s compareAndSet from that point
    //on. Since nothing else ever called release() for a captcha that had actually been shown (onCompletion()
    //below didn't, and uninject() explicitly skips a shown captcha's cleanup entirely), this meant the
    //backing ByteBuf[] (built from CaptchaImageGenerator, one buffer per rendered "pixel" - can be hundreds)
    //was never explicitly released for virtually every connection actually shown this captcha type (the
    //default). Not a hard leak - Netty's default unpooled direct-buffer allocator registers a JVM Cleaner
    //fallback - but release is no longer deterministic/prompt, adding avoidable off-heap pressure under a
    //sustained bot-connection flood (exactly this captcha's own reason to exist).
    private final AtomicBoolean buffersFreed = new AtomicBoolean();

    public SmoothCaptcha() {
        BufferedImage image = CaptchaImageGenerator.generateCaptchaImageX256(captcha, maxRotation, false, false);
        this.buffers = ImageRenderer.smoothModelBuffers(image);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        this.shown.set(true);
        //created to spread out the packet sending, even just a bit
        user.writeAndFlushWithThresholdSilently(this.buffers, Math.max(this.buffers.length / 5 + 1, 100));
        //Main.logInfo("SENT CAPTCHAAA " + buffers.length);
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        int idStart = ImageRenderer.ENTITY_ID_START + 1;

        user.writeAndFlushSilently(OutEntityDestroyPacketConstructor.constructDynamic(idStart, this.buffers.length / 3));
        this.release();
    }

    @Override
    public void release() {
        if (this.buffersFreed.compareAndSet(false, true))
            for (ByteBuf buf : buffers) buf.release();
    }

    @Override
    protected boolean isReleased() {
        return this.shown.get();
    }
}