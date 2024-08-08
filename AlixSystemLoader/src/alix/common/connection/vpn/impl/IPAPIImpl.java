package alix.common.connection.vpn.impl;

import alix.common.connection.vpn.CheckResult;
import alix.common.connection.vpn.ProxyCheck;
import com.google.gson.JsonElement;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class IPAPIImpl implements ProxyCheck {

    private final AtomicLong nextRequestCountReset = new AtomicLong();
    private final AtomicInteger requests = new AtomicInteger();

    @Override
    public CheckResult isProxy(String address) {
        int req;
        if (this.nextRequestCountReset.get() < System.currentTimeMillis()) {
            this.requests.set(0);
            req = 0;
        } else req = requests.get();

        switch (req) {
            case 0:
                this.nextRequestCountReset.set(System.currentTimeMillis() + 45000L);
                break;
            case 44:
                return CheckResult.UNAVAILABLE;
        }

        this.requests.getAndIncrement();

        JsonElement out = ProxyCheck.getResponse("http://ip-api.com/json/" + address + "?fields=proxy");//currently does not include "status"
        if (out == null) return CheckResult.UNAVAILABLE;

        JsonElement proxy = out.getAsJsonObject().get("proxy");
        if (proxy == null) return CheckResult.UNAVAILABLE;

        return proxy.getAsBoolean() ? CheckResult.PROXY : CheckResult.NON_PROXY;
    }
}