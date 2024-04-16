package shadow.utils.holders.packet.buffered;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetExperience;
import io.netty.buffer.ByteBuf;
import shadow.utils.main.AlixUtils;
import shadow.utils.netty.NettyUtils;

public final class BufferedPackets {

    public static final int EXPERIENCE_UPDATES_PER_SECOND = 5;
    public static final int captchaPacketArraySize = AlixUtils.maxCaptchaTime * EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 1
    public static final int loginPacketArraySize = AlixUtils.maxLoginTime * EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 1
    public static final ByteBuf[] captchaOutExperiencePackets = AlixUtils.requireCaptchaVerification ? new ByteBuf[captchaPacketArraySize] : null;
    public static final ByteBuf[] loginOutExperiencePackets = new ByteBuf[loginPacketArraySize];

    public static void init() {
        for (int i = 0; i < loginPacketArraySize; i++) {

            float xpBar = ((float) i) / loginPacketArraySize;
            int lvl = i / EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x;
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            loginOutExperiencePackets[i] = NettyUtils.constBuffer(new WrapperPlayServerSetExperience(xpBar, lvl, totalExp)); //ReflectionUtils.outExperienceConstructor.newInstance(xpBar, totalExp, lvl);
        }

        if (!AlixUtils.requireCaptchaVerification) return;

        for (int i = 0; i < captchaPacketArraySize; i++) {

            float xpBar = ((float) i) / captchaPacketArraySize;
            int lvl = i / EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x;
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            captchaOutExperiencePackets[i] = NettyUtils.constBuffer(new WrapperPlayServerSetExperience(xpBar, lvl, totalExp));// ReflectionUtils.outExperienceConstructor.newInstance(xpBar, totalExp, lvl);
        }
    }

    private BufferedPackets() {
    }
}