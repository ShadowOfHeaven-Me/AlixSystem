package shadow.systems.login.result;

import alix.common.login.LoginVerdict;
import alix.common.data.PersistentUserData;

import java.net.InetAddress;

public final class LoginInfo {

    //public static final LoginInfo PREMIUM_LOGIN = new LoginInfo(LoginVerdict.LOGIN_PREMIUM, null, null);
    private final LoginVerdict verdict;
    private final String strIP;
    private final InetAddress ip;
    private final PersistentUserData data;
    //private final PacketInterceptor packetInterceptor;
    //final long removalTime;

    LoginInfo(LoginVerdict verdict, InetAddress ip, String strIP, PersistentUserData data) {
        this.verdict = verdict;
        this.ip = ip;
        this.strIP = strIP;
        this.data = data;
        //this.packetInterceptor = interceptor;
        //this.removalTime = System.currentTimeMillis() + 60000;
    }

    public LoginVerdict getVerdict() {
        return verdict;
    }

    public InetAddress getIP() {
        return ip;
    }

    public String getTextIP() {
        return strIP;
    }

    public PersistentUserData getData() {
        return data;
    }
}