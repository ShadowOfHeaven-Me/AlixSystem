package shadow.systems.login.captcha.manager;

import io.netty.channel.Channel;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.filter.packet.types.PacketBlocker;

public final class CountdownTask {//show count down and kick out

    private static final int packetArraySize = AlixUtils.maxCaptchaTime * 10; //todo: proportional 1
    private static final Object[] outExperiencePackets = AlixUtils.requireCaptchaVerification ? new Object[packetArraySize] : null;
    private final Channel channel;
    private int index;

    public CountdownTask(Channel channel) {
        this.channel = channel;
        this.index = packetArraySize;
    }

    //Returns: Whether the player should be kicked
    public boolean tick() {
        if (index == 0) return true;
        this.channel.writeAndFlush(outExperiencePackets[--this.index]);
        return false;
    }

    public static void pregenerate() {
        if (!AlixUtils.requireCaptchaVerification) return;
        for (int i = 0; i < packetArraySize; i++) {

            float xpBar = ((float) i) / packetArraySize;
            int lvl = i / 10; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x; now x = 10
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            try {
                outExperiencePackets[i] = ReflectionUtils.outExperienceConstructor.newInstance(xpBar, totalExp, lvl);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }
}