package shadow.systems.login.vpn.impl;

import com.google.gson.JsonElement;
import shadow.systems.login.vpn.CheckResult;
import shadow.systems.login.vpn.ProxyCheck;

public final class IP2ProxyImpl implements ProxyCheck {

    @Override
    public CheckResult isProxy(String address) {
        JsonElement out = ProxyCheck.getResponse("https://api.ip2location.io/?ip=" + address);
        if (out == null) return CheckResult.UNAVAILABLE;

        JsonElement proxy0 = out.getAsJsonObject().get("is_proxy");
        if (proxy0 == null) return CheckResult.UNAVAILABLE;

        boolean proxy = proxy0.getAsBoolean();
        return proxy ? CheckResult.PROXY : CheckResult.NORMAL;
    }
}