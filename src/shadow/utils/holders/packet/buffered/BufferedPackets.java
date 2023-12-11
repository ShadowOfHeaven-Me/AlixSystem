package shadow.utils.holders.packet.buffered;

import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.main.AlixUtils;

public final class BufferedPackets {

    private static final Object[] anvilAllItemsPackets = new Object[128];
    private static final Object[] anvilFirstTwoItemsPackets = new Object[128];

    public static final int captchaPacketArraySize = AlixUtils.maxCaptchaTime * 10; //todo: proportional 1
    public static final int loginPacketArraySize = AlixUtils.maxLoginTime * 10; //todo: proportional 1
    public static final Object[] captchaOutExperiencePackets = AlixUtils.requireCaptchaVerification ? new Object[captchaPacketArraySize] : null;
    public static final Object[] loginOutExperiencePackets = new Object[loginPacketArraySize];

    public static Object getAllItemsPacketOf(int id) {
        return id < 129 ? anvilAllItemsPackets[id - 1] : null;
    }

    public static Object getInvalidIndicatePacketOf(int id) {
        return id < 129 ? anvilFirstTwoItemsPackets[id - 1] : null;
    }

    public static void init(Object ALL_ITEMS_LIST, Object INVALID_INDICATE_ITEMS_LIST) {
        for (int i = 0; i < 128; i++) {
            anvilAllItemsPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, ALL_ITEMS_LIST);
            anvilFirstTwoItemsPackets[i] = OutWindowItemsPacketConstructor.construct0(i + 1, 3, INVALID_INDICATE_ITEMS_LIST);
        }
    }

    public static void init() {
        for (int i = 0; i < loginPacketArraySize; i++) {

            float xpBar = ((float) i) / loginPacketArraySize;
            int lvl = i / 10; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x; now x = 10
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
            int lvl = i / 10; //todo: proportional 2
            // /\ proportional to the repeating captcha task manager delay & packet array size: lvl = i / x; & delay = 1000 millis / x; packetArraySize = captcha time * x; now x = 10
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