package alix.common.connection.vpn.impl;

import alix.common.connection.vpn.CheckResultHolder;
import alix.common.connection.vpn.IPInfo;
import alix.common.connection.vpn.ProxyCheck;
import alix.common.connection.vpn.ProxyType;
import alix.common.connection.vpn.utils.SlidingWindowRateLimiter;
import alix.common.connection.vpn.utils.RateLimiter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

public final class IPAPIImpl implements ProxyCheck {

    // 45 req/min
    private final RateLimiter rateLimiter = new SlidingWindowRateLimiter(45, 1, TimeUnit.MINUTES);

    @Override
    public CheckResultHolder isProxy(String address) {
        if (!rateLimiter.tryAcquire()) return CheckResultHolder.UNAVAILABLE;

        JsonElement out = ProxyCheck.getResponse("http://ip-api.com/json/" + address + "?fields=status,country,isp,as,proxy,hosting");
        if (out == null || !out.isJsonObject()) return CheckResultHolder.UNAVAILABLE;

        JsonObject obj = out.getAsJsonObject();
        if (!ProxyCheck.has(obj, "status") || !"success".equals(obj.get("status").getAsString()))
            return CheckResultHolder.UNAVAILABLE;

        boolean isProxyFlag = ProxyCheck.has(obj, "proxy") && obj.get("proxy").getAsBoolean();
        boolean isHosting = ProxyCheck.has(obj, "hosting") && obj.get("hosting").getAsBoolean();
        boolean isBad = isProxyFlag || isHosting;

        IPInfo.Builder builder = new IPInfo.Builder(address, "ip-api.com")
                .proxy(isBad)
                .isHosting(isHosting)
                .isVpn(isProxyFlag && !isHosting);

        if (ProxyCheck.has(obj, "country")) builder.country(obj.get("country").getAsString());
        if (ProxyCheck.has(obj, "isp")) builder.isp(obj.get("isp").getAsString());
        if (ProxyCheck.has(obj, "as")) builder.asn(obj.get("as").getAsString());

        if (isHosting) builder.proxyType(ProxyType.DATACENTER);
        else if (isProxyFlag) builder.proxyType(ProxyType.VPN);
        else builder.proxyType(ProxyType.NOT_A_PROXY);

        IPInfo info = builder.build();
        return isBad ? CheckResultHolder.proxy(info) : CheckResultHolder.nonProxy(info);
    }
}