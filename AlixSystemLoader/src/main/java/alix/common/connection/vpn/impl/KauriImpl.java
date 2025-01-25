package alix.common.connection.vpn.impl;

import alix.common.connection.vpn.CheckResult;
import alix.common.connection.vpn.ProxyCheck;
import com.google.gson.JsonElement;

public final class KauriImpl implements ProxyCheck {

    @Override
    public CheckResult isProxy(String address) {
        JsonElement out = ProxyCheck.getResponse("https://funkemunky.cc/vpn?ip=" + address);
        if (out == null) return CheckResult.UNAVAILABLE;

        JsonElement proxy0 = out.getAsJsonObject().get("proxy");
        if (proxy0 == null) return CheckResult.UNAVAILABLE;

        boolean proxy = proxy0.getAsBoolean();
        return proxy ? CheckResult.PROXY : CheckResult.NON_PROXY;
    }
}