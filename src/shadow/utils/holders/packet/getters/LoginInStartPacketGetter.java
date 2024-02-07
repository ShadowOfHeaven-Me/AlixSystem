package shadow.utils.holders.packet.getters;

import alix.common.utils.other.throwable.AlixException;
import com.mojang.authlib.GameProfile;
import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Method;

public final class LoginInStartPacketGetter {

    private static final Method loginInStartGameProfileMethod;
    private static final boolean newer;

    static {
        Class<?> packetClazz = ReflectionUtils.loginInStartPacketClass;
        Method method = null;
        boolean n = false;//initialization is necessary because of the compiler
        try {
            method = ReflectionUtils.getMethodByReturnType(packetClazz, GameProfile.class);
            n = false;
        } catch (Throwable e) {
            for (Method m : packetClazz.getMethods()) {
                if (m.getReturnType() == String.class && !m.getName().equals("toString")) {
                    method = m;
                    n = true;
                    break;
                }
            }
        }
        if (method == null) throw new AlixException();
        newer = n;
        loginInStartGameProfileMethod = method;
    }

    public static String getPlayerName(Object packet) throws Exception {
        Object obj = loginInStartGameProfileMethod.invoke(packet);
        return newer ? (String) obj : ((GameProfile) obj).getName();
    }

    public static void init() {
    }

    private LoginInStartPacketGetter() {
    }
}