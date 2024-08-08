package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.CaptchaRenderer;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SmoothCaptcha extends Captcha {

    private final ByteBuf[] buffers;
    private final AtomicBoolean released = new AtomicBoolean();

    public SmoothCaptcha() {
        BufferedImage image = CaptchaImageGenerator.generateCaptchaImageX256(captcha, maxRotation, false, false);
        this.buffers = CaptchaRenderer.smoothModelBuffers(image, 256);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        this.released.set(true);
        for (ByteBuf buf : buffers) user.writeSilently(buf);
        user.flush();
        //Main.logInfo("SENT CAPTCHAAA " + buffers.length);
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        int[] ids = new int[this.buffers.length / 3];
        int idStart = CaptchaRenderer.ENTITY_ID_START + 1;

        for (int i = 0; i < ids.length; i++) ids[i] = idStart + i;

        WrapperPlayServerDestroyEntities wrapper = new WrapperPlayServerDestroyEntities(ids);
        user.writeAndFlushSilently(NettyUtils.createBuffer(wrapper));
    }

    @Override
    public void release() {
        if (this.released.compareAndSet(false, true))
            for (ByteBuf buf : buffers) buf.release();
    }
}