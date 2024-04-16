package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.ScheduledFuture;
import org.bukkit.Location;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.captcha.ParticleRenderer;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public final class ParticleCaptcha extends Captcha {

    private static final Location CENTER = AlixWorld.TELEPORT_LOCATION.clone().add(0, -2, -2.5);
    private final ByteBuf[] buffers;
    //private final ByteBuf buffer;
    private volatile ScheduledFuture<?> task;
    private volatile boolean cancelled, cleanedUp;

    public ParticleCaptcha() {
        BufferedImage image = CaptchaImageGenerator.generateCaptchaImage(captcha, maxRotation, false, false, CaptchaImageGenerator::getParticleColor);//aliasing is necessary for us, since it shows more contrast
        this.buffers = ParticleRenderer.renderingBuffers(image, CENTER);//Unreleasable(ReadOnly(Direct)))
        //Main.logError("BUFFERS HASH: " + ParticleRenderer.renderingBuffers(image, CENTER).length);// + " NORMALLY: " + ParticleRenderer.list(image, CENTER).length);
        //this.buffer = Unpooled.buffer();
        //for (ByteBuf buf : buffers) buffer.writeBytes(buf);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        //Main.logError("SENT: ");
        if (cancelled) return;
        this.task = user.getChannel().eventLoop().scheduleAtFixedRate(() -> {
            if (cancelled) return;
            //long t = System.nanoTime();
            for (ByteBuf buf : buffers) user.writeSilently(buf.duplicate());
            //Main.logError("TIME: " + (System.nanoTime() - t) / Math.pow(10, 6) + "ms");
        }, 0, 400, TimeUnit.MILLISECONDS);
    }

    @Override
    public void uninject() {
        if (cancelled) return;
        this.cancelled = true;
        try {
            if (task != null) this.task.cancel(true);
        } finally {
            //Main.logInfo("Released " + buffers[0].refCnt());
            //Should always be 1 and work, but you never know
            int refCnt;
            for (ByteBuf buf : buffers)
                if ((refCnt = buf.refCnt()) != 0)
                    buf.unwrap().release(refCnt);//unwrap, since it's wrapped with an unreleasable buffer
            //this.buffer.release();
        }
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        this.uninject();
        //ReflectionUtils.sendBlindnessPackets(user);
    }

/*    @Override
    protected void finalize() throws Throwable {//On reload deallocation primitive fix
        this.uninject();
        super.finalize();
    }*/
}