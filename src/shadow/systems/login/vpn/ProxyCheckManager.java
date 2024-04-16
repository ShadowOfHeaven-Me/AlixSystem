package shadow.systems.login.vpn;

import alix.common.utils.collections.list.LoopList;
import shadow.systems.login.vpn.impl.IP2ProxyImpl;
import shadow.systems.login.vpn.impl.KauriImpl;
import shadow.systems.login.vpn.impl.ProxyCheckIOImpl;

public final class ProxyCheckManager {

    public static final ProxyCheckManager INSTANCE = new ProxyCheckManager();
    private final LoopList<ProxyCheck> list;
    private volatile ProxyCheck check;
    //private boolean warned;

    private ProxyCheckManager() {//request max:     1000/day             500/day       20,000/month
        this.list = LoopList.newConcurrent(new ProxyCheckIOImpl(), new IP2ProxyImpl(), new KauriImpl());
        this.check = list.get(0);
    }

    public boolean isProxy(String address) {
        CheckResult result;
        int looped = 0;
        while ((result = check.isProxy(address)) == CheckResult.UNAVAILABLE) {
            this.check = list.next();
            if (++looped == list.size())//we looped through every provider in this very loop, all of which returned unavailable. It's time to panic
                return false;//we just gotta let everyone in
        }
        return result == CheckResult.PROXY;
    }

    /*if (!warned) {
                    Main.logWarning("");
                    Main.logWarning("[WARNING]");
                    Main.logWarning("");
                    Main.logWarning("All VPN providers are unavailable! No VPN checks can be made any more!");
                    Main.logWarning("");
                    Main.logWarning("[WARNING]");
                    Main.logWarning("");
                }
                this.warned = true;*/
}