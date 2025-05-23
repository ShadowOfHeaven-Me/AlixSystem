package shadow.systems.login.captcha.subtypes;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerClearTitles;
import io.netty.buffer.ByteBuf;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.misc.packet.constructors.OutTitlePacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;

import static shadow.utils.main.AlixUtils.maxCaptchaTime;

public final class SubtitleCaptcha extends Captcha {

    private static final ByteBuf resetTitleBuffer = NettyUtils.constBuffer(new WrapperPlayServerClearTitles(true));
    private final ByteBuf[] buffers = OutTitlePacketConstructor.constructDynamic("Captcha: ", "§c" + captcha, 0, maxCaptchaTime * 20, 0);

/*    public SubtitleCaptcha(Captcha captcha) {
        super(captcha);
    }*/

    @Override
    //@Dependent(clazz = VirtualCountdown.class, method = "#tick", reason = "Flush already invoked every 500 ms")
    public void sendPackets(UnverifiedUser user) {
        for (ByteBuf buf : buffers) user.writeSilently(buf);
        user.flush();
    }

    @Override
    protected boolean isReleased() {
        return true;
    }

    @Override
    public void onCompletion(UnverifiedUser user) {
        user.writeAndFlushConstSilently(resetTitleBuffer);
    }
}