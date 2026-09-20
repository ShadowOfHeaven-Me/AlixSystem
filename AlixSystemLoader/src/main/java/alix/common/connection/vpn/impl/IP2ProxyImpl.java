package alix.common.connection.vpn.impl;

import alix.common.connection.vpn.CheckResultHolder;
import alix.common.connection.vpn.IPInfo;
import alix.common.connection.vpn.ProxyCheck;
import alix.common.connection.vpn.ProxyType;
import alix.common.connection.vpn.utils.DailyUTCRateLimiter;
import alix.common.connection.vpn.utils.RateLimiter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class IP2ProxyImpl implements ProxyCheck {

    // IP2Location unauthenticated limit is 1,000 queries per day, resets at 0:00 UTC
    private final RateLimiter rateLimiter = new DailyUTCRateLimiter(1000);

    @Override
    public CheckResultHolder isProxy(String address) {
        if (!rateLimiter.tryAcquire()) return CheckResultHolder.UNAVAILABLE;

        JsonElement out = ProxyCheck.getResponse("https://api.ip2location.io/?ip=" + address);
        if (out == null || !out.isJsonObject()) return CheckResultHolder.UNAVAILABLE;

        JsonObject obj = out.getAsJsonObject();

        // If the API rate limit is exceeded on their end, they might return an error json without this key
        if (!ProxyCheck.has(obj, "is_proxy")) return CheckResultHolder.UNAVAILABLE;

        boolean isProxy = obj.get("is_proxy").getAsBoolean();
        IPInfo.Builder builder = new IPInfo.Builder(address, "IP2Location")
                .proxy(isProxy);

        if (ProxyCheck.has(obj, "country_name")) builder.country(obj.get("country_name").getAsString());

        // The API returns the company/ISP under the "as" key
        if (ProxyCheck.has(obj, "as")) builder.isp(obj.get("as").getAsString());

        // ASN is returned as just the number (e.g., "13335")
        if (ProxyCheck.has(obj, "asn")) {
            String asn = obj.get("asn").getAsString();
            builder.asn(asn.startsWith("AS") ? asn : "AS" + asn);
        }

        // Only premium/higher tier queries or certain proxy IPs return the specific proxy_type string
        if (ProxyCheck.has(obj, "proxy_type") && !obj.get("proxy_type").getAsString().isEmpty()) {
            String pType = obj.get("proxy_type").getAsString();
            ProxyType type = switch (pType) {
                case "VPN" -> ProxyType.VPN;
                case "TOR" -> ProxyType.TOR;
                case "DCH" -> ProxyType.DATACENTER;
                case "PUB", "WEB" -> ProxyType.PUBLIC_PROXY;
                case "SES" -> ProxyType.SCRAPER; // Search Engine Spiders
                default -> isProxy ? ProxyType.PROXY : ProxyType.NOT_A_PROXY;
            };
            builder.proxyType(type)
                    .isVpn(type == ProxyType.VPN)
                    .isTor(type == ProxyType.TOR)
                    .isHosting(type == ProxyType.DATACENTER)
                    .isScraper(type == ProxyType.SCRAPER);
        } else {
            builder.proxyType(isProxy ? ProxyType.PROXY : ProxyType.NOT_A_PROXY);
        }

        IPInfo info = builder.build();
        return isProxy ? CheckResultHolder.proxy(info) : CheckResultHolder.nonProxy(info);
    }
}
