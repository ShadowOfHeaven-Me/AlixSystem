package shadow.systems.login.result;

import alix.common.data.PersistentUserData;
import alix.common.login.LoginVerdict;

import java.net.InetAddress;

public final class LoginInfo {

    //public static final LoginInfo PREMIUM_LOGIN = new LoginInfo(LoginVerdict.LOGIN_PREMIUM, null, null);
    //private static final AtomicReferenceFieldUpdater<LoginInfo, LoginVerdict> UPDATER_VERDICT = AtomicReferenceFieldUpdater.newUpdater(LoginInfo.class, LoginVerdict.class, "verdict");
    //private static final AtomicReferenceFieldUpdater<LoginInfo, PersistentUserData> UPDATER_DATA = AtomicReferenceFieldUpdater.newUpdater(LoginInfo.class, PersistentUserData.class, "data");
    private final String strIP;
    private final InetAddress ip;
    private final PersistentUserData data;
    private final LoginVerdict verdict;
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

/*    public void setVerdict(LoginVerdict verdict) {
        UPDATER_VERDICT.set(this, verdict);
    }

    public void setData(PersistentUserData data) {
        UPDATER_DATA.set(this, data);
    }*/
}