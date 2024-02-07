package shadow.utils.holders.packet.buffered;

import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.main.AlixUtils;

public final class BufferedPackets {

    private static final int EXPERIENCE_UPDATES_PER_SECOND = 5;
    public static final int captchaPacketArraySize = AlixUtils.maxCaptchaTime * EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 1
    public static final int loginPacketArraySize = AlixUtils.maxLoginTime * EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 1
    public static final Object[] captchaOutExperiencePackets = AlixUtils.requireCaptchaVerification ? new Object[captchaPacketArraySize] : null;
    public static final Object[] loginOutExperiencePackets = new Object[loginPacketArraySize];

    public static void init() {
        for (int i = 0; i < loginPacketArraySize; i++) {

            float xpBar = ((float) i) / loginPacketArraySize;
            int lvl = i / EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x;
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            try {
                loginOutExperiencePackets[i] = ReflectionUtils.outExperienceConstructor.newInstance(xpBar, totalExp, lvl);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        if (!AlixUtils.requireCaptchaVerification) return;

        for (int i = 0; i < captchaPacketArraySize; i++) {

            float xpBar = ((float) i) / captchaPacketArraySize;
            int lvl = i / EXPERIENCE_UPDATES_PER_SECOND; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x;
            int totalExp = lvl <= 16 ? lvl * lvl + 6 * lvl : lvl <= 30 ? 5 * lvl - 38 : 9 * lvl - 158; //from the minecraft wiki

            try {
                captchaOutExperiencePackets[i] = ReflectionUtils.outExperienceConstructor.newInstance(xpBar, totalExp, lvl);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    private BufferedPackets() {
    }
}