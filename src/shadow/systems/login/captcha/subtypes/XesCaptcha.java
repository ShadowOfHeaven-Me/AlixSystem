package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.CaptchaRenderer;
import shadow.utils.users.types.UnverifiedUser;

import java.awt.image.BufferedImage;

public final class XesCaptcha extends Captcha {

    private final ByteBuf[] buffers;

    public XesCaptcha() {
        BufferedImage image = CaptchaImageGenerator.generateCaptchaImageX256(captcha, maxRotation, false, false);
        this.buffers = CaptchaRenderer.xes(image);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        for (ByteBuf buf : buffers) user.writeSilently(buf);
        user.flushSilently();
    }
}