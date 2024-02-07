package shadow.systems.login.vpn.impl;

import com.google.gson.JsonElement;
import shadow.Main;
import shadow.systems.login.vpn.CheckResult;
import shadow.systems.login.vpn.ProxyCheck;

public final class ProxyCheckIOImpl implements ProxyCheck {

    @Override
    public CheckResult isProxy(String address) {
        JsonElement out = ProxyCheck.getResponse("https://proxycheck.io/v2/" + address + "?vpn=1&asn=1");
        //Main.logError("REQUEST: " + "https://proxycheck.io/v2/" + address + "?vpn=1&asn=1");
        if (out == null) return CheckResult.UNAVAILABLE;

        //Main.logWarning("" + out);

        JsonElement proxy0 = out.getAsJsonObject().get(address);
        if (proxy0 == null) return CheckResult.UNAVAILABLE;

        JsonElement proxy1 = proxy0.getAsJsonObject().get("proxy");
        if (proxy1 == null) return CheckResult.UNAVAILABLE;

        String proxy = proxy1.getAsString();
        return proxy.equals("yes") ? CheckResult.PROXY : CheckResult.NORMAL;
    }
}