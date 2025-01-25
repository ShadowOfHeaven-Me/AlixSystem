package shadow.utils.misc.packet.getters;

/*
public final class LoginInStartPacketGetter {

*/
/*    private static final Method loginInStartNameMethod;
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
        loginInStartNameMethod = method;
    }

    public static String getPlayerName(Object packet) throws Exception {
        Object obj = loginInStartNameMethod.invoke(packet);
        return newer ? (String) obj : ((GameProfile) obj).getName();
    }*//*



    public static void init() {
    }

    private LoginInStartPacketGetter() {
    }
}
*/
