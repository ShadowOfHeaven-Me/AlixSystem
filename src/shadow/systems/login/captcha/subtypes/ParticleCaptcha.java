package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import alix.common.scheduler.AlixScheduler;
import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.ScheduledFuture;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.netty.NettyUtils;
import shadow.utils.netty.unsafe.UnsafeNettyUtils;
import shadow.utils.netty.unsafe.raw.RawAlixPacket;
import shadow.utils.users.types.UnverifiedUser;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public final class ParticleCaptcha extends Captcha {

    private final ByteBuf[] buffers;
    //private final ByteBuf buffer;
    private ScheduledFuture<?> task;
    private boolean cancelled;//, cleanedUp;
    private RawAlixPacket packet;

    public ParticleCaptcha() {
        BufferedImage image = CaptchaImageGenerator.generateCaptchaImage(captcha, maxRotation, false, false);//aliasing is necessary for us, since it shows more contrast
        this.buffers = ImageRenderer.particleBuffers(image);//Unreleasable(ReadOnly(Direct)))
        //Main.logError("CREATED: " + buffers.length);
        //Main.logError("BUFFERS HASH: " + ParticleRenderer.captchaRenderingBuffers(image).length + " NORMALLY: " + ParticleRenderer.list(image).length);
        //this.buffer = Unpooled.buffer();
        //for (ByteBuf buf : buffers) buffer.writeBytes(buf);
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        //Main.logError("SENT: " + user.silentContext().pipeline().names());
        if (cancelled) return;

        //already on the eventLoop
        //long t0 = System.nanoTime();
        UnsafeNettyUtils.sendAndSetRaw(user.silentContext(), user.bufHarvester, b -> b, this.buffers);
        //int size = 0;
        //for (ByteBuf buf : buffers) size += buf.readableBytes();
        //Main.logError("OF SIZE: " + size);
        //long diff0 = System.nanoTime() - t0;
        //Main.logError("ALLOCATING TIME: " + diff0 / Math.pow(10, 6) + "ms");

        //no need to make it direct, since that here only dictates what ByteBuf is preferred when allocating unspecified
        AlixScheduler.async(() -> {
            this.packet = RawAlixPacket.of(user.getChannel(), this.buffers, NettyUtils::constBuffer, buf -> buf.unwrap().release());

            //no need to release the array, since it's already done so per the CompositeByteBuf#addComponents method
            //for (ByteBuf buf : buffers) buf.release(buf.refCnt());

            this.task = user.getChannel().eventLoop().scheduleAtFixedRate(() -> {
                if (cancelled) return;
                //long t = System.nanoTime();

                //for (ByteBuf buf : buffers)
                //Main.logError("INDEX " + buf.get().component(0).readerIndex() + " " + buf.get().component(0).writerIndex());
                this.packet.write();//the flush is already performed quite often by the VerificationReminder

                //long diff = System.nanoTime() - t;
                //Main.logError("TIME: " + diff / Math.pow(10, 6) + "ms");
            }, 300L, 300L, TimeUnit.MILLISECONDS);
        });
    }

    @Override
    public void release() {
        //Main.logError("UNINJECTED " + Arrays.toString(Thread.currentThread().getStackTrace()));
        //AlixUnsafe.getUnsafe().throwException(new AlixException(""));
        if (cancelled) return;
        this.cancelled = true;
        try {
            if (task != null) this.task.cancel(true);
        } finally {
            if (this.packet != null) this.packet.release();
            else for (ByteBuf buf : buffers) buf.release();
        }
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        this.release();
        //ReflectionUtils.sendBlindnessPackets(user);
    }

    //Encodes by all
/*    public static ByteBuf encodeByAll(ByteBuf original, ChannelHandlerContext silentContext) {
        ByteBuf out = original;
        for (String name : silentContext.pipeline().names()) {
            if (name.equals(silentContext.name())) return out;
            ChannelHandlerContext ctx = silentContext.pipeline().context(name);
            ChannelHandler handler = ctx.handler();
            Main.logError("HANDLER: " + handler.getClass().getSimpleName() + " " + name + " BYTE " + (handler instanceof MessageToByteEncoder) + " MESSAGE: " + (handler instanceof MessageToMessageEncoder));
            if(handler instanceof MessageToMessageEncoder) {
                Main.logError("HANDLER IS MESSAGE ENCODER!");
                Method method = ReflectionUtils.getMethod(handler.getClass(), "encode", ChannelHandlerContext.class, ByteBuf.class, List.class);
                method.invoke(handler, ctx, out, original)
            }
            if (handler instanceof MessageToByteEncoder) {
                Main.logError("HANDLER IS BYTE ENCODER!");
                Method method = ReflectionUtils.getMethod(handler.getClass(), "encode", ChannelHandlerContext.class, ByteBuf.class, ByteBuf.class);

            }
        }
        return out;
    }*/

/*    @Override
    protected void finalize() throws Throwable {//On reload deallocation primitive fix
        this.uninject();
        super.finalize();
    }*/
}