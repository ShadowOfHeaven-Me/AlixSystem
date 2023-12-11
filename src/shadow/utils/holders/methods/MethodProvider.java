package shadow.utils.holders.methods;

import shadow.utils.holders.methods.impl.UserKickMethod;

public final class MethodProvider {

    //private static final MethodProvider instance = new MethodProvider();
    private static final UserKickMethod kickMethod = UserKickMethod.createImpl();

    public static UserKickMethod getKickMethod() {
        return kickMethod;
    }
}