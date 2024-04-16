package shadow.systems.login.result;

import shadow.utils.objects.savable.data.PersistentUserData;

public final class LoginInfo {

    //public static final LoginInfo PREMIUM_LOGIN = new LoginInfo(LoginVerdict.LOGIN_PREMIUM, null, null);
    private final LoginVerdict verdict;
    private final String ip;
    private final PersistentUserData data;
    //private final PacketInterceptor packetInterceptor;
    //final long removalTime;

    LoginInfo(LoginVerdict verdict, String ip, PersistentUserData data) {
        this.verdict = verdict;
        this.ip = ip;
        this.data = data;
        //this.packetInterceptor = interceptor;
        //this.removalTime = System.currentTimeMillis() + 60000;
    }

    public LoginVerdict getVerdict() {
        return verdict;
    }

    public String getIP() {
        return ip;
    }

    public PersistentUserData getData() {
        return data;
    }
}