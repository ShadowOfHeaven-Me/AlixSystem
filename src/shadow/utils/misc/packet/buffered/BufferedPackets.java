package shadow.utils.misc.packet.buffered;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetExperience;
import io.netty.buffer.ByteBuf;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.NettyUtils;

public final class BufferedPackets {

    public static final int EXPERIENCE_UPDATES_PER_SECOND = 2;
    public static final int captchaPacketArraySize = AlixUtils.maxCaptchaTime * EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 1
    public static final int loginPacketArraySize = AlixUtils.maxLoginTime * EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 1
    public static final ByteBuf[] captchaOutExperiencePackets = AlixUtils.requireCaptchaVerification ? new ByteBuf[captchaPacketArraySize] : null;
    public static final ByteBuf[] loginOutExperiencePackets = new ByteBuf[loginPacketArraySize];

    public static void init() {
        preGen(loginOutExperiencePackets);

        if (AlixUtils.requireCaptchaVerification) preGen(captchaOutExperiencePackets);
    }

    private static void preGen(ByteBuf[] buffers) {
        for (int i = 0; i < buffers.length; i++) {

            float xpBar = ((float) i) / buffers.length;
            int lvl = i / EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x;
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            buffers[i] = NettyUtils.constBuffer(new WrapperPlayServerSetExperience(xpBar, lvl, totalExp)); //ReflectionUtils.outExperienceConstructor.newInstance(xpBar, totalExp, lvl);
        }
    }


    private BufferedPackets() {
    }
}