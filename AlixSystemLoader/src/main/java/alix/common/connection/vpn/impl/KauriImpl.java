package alix.common.connection.vpn.impl;

import alix.common.connection.vpn.CheckResultHolder;
import alix.common.connection.vpn.IPInfo;
import alix.common.connection.vpn.ProxyCheck;
import alix.common.connection.vpn.ProxyType;
import alix.common.connection.vpn.utils.DynamicMonthlyRateLimiter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class KauriImpl implements ProxyCheck {

    // 20,000 queries per month
    private final DynamicMonthlyRateLimiter rateLimiter = new DynamicMonthlyRateLimiter(20_000);

    @Override
    public CheckResultHolder isProxy(String address) {
        if (!rateLimiter.tryAcquire()) return CheckResultHolder.UNAVAILABLE;

        JsonElement out = ProxyCheck.getResponse("https://funkemunky.cc/vpn?ip=" + address);
        if (out == null || !out.isJsonObject()) return CheckResultHolder.UNAVAILABLE;

        JsonObject obj = out.getAsJsonObject();

        // Sync our local rate limit counter with the definitive API value
        if (ProxyCheck.has(obj, "queriesLeft")) {
            rateLimiter.sync(obj.get("queriesLeft").getAsInt());
        }

        if (!ProxyCheck.has(obj, "proxy")) return CheckResultHolder.UNAVAILABLE;

        boolean isProxy = obj.get("proxy").getAsBoolean();
        IPInfo.Builder builder = new IPInfo.Builder(address, "Kauri (Funkemunky)")
                .proxy(isProxy);

        if (ProxyCheck.has(obj, "countryName")) {
            String country = obj.get("countryName").getAsString();
            if (!"unknown".equalsIgnoreCase(country)) builder.country(country);
        }

        if (ProxyCheck.has(obj, "isp")) {
            String isp = obj.get("isp").getAsString();
            if (!"unknown".equalsIgnoreCase(isp)) builder.isp(isp);
        } else if (ProxyCheck.has(obj, "organization")) {
            // Fallback to organization if ISP is missing or 'unknown'
            String org = obj.get("organization").getAsString();
            if (!"unknown".equalsIgnoreCase(org)) builder.isp(org);
        }

        if (ProxyCheck.has(obj, "asn")) {
            String asn = obj.get("asn").getAsString();
            if (!"unknown".equalsIgnoreCase(asn)) {
                builder.asn(asn.startsWith("AS") ? asn : "AS" + asn);
            }
        }

        // Kauri does not currently specify proxy subtypes, so we fall back to generic types
        builder.proxyType(isProxy ? ProxyType.PROXY : ProxyType.NOT_A_PROXY);

        IPInfo info = builder.build();
        return isProxy ? CheckResultHolder.proxy(info) : CheckResultHolder.nonProxy(info);
    }
}