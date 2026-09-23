package alix.common.connection.vpn.impl;

import alix.common.connection.vpn.CheckResultHolder;
import alix.common.connection.vpn.IPInfo;
import alix.common.connection.vpn.ProxyCheck;
import alix.common.connection.vpn.ProxyType;
import alix.common.connection.vpn.utils.DailyUTCRateLimiter;
import alix.common.connection.vpn.utils.RateLimiter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ProxyCheckIOImpl implements ProxyCheck {

    // ProxyCheck explicitly requests a cap of 100/day on the free tier, resetting at UTC 0:00
    private final RateLimiter rateLimiter = new DailyUTCRateLimiter(100);

    @Override
    public CheckResultHolder isProxy(String address) {
        if (!rateLimiter.tryAcquire()) return CheckResultHolder.UNAVAILABLE;

        JsonElement out = ProxyCheck.getResponse("https://proxycheck.io/v3/" + address);
        if (out == null || !out.isJsonObject()) return CheckResultHolder.UNAVAILABLE;

        JsonObject root = out.getAsJsonObject();

        if (!ProxyCheck.has(root, "status") || !"ok".equalsIgnoreCase(root.get("status").getAsString())) {
            return CheckResultHolder.UNAVAILABLE;
        }

        JsonElement ipElement = root.get(address);
        if (ipElement == null || !ipElement.isJsonObject()) return CheckResultHolder.UNAVAILABLE;

        JsonObject ipObj = ipElement.getAsJsonObject();
        IPInfo.Builder builder = new IPInfo.Builder(address, "proxycheck.io (v3)");

        // 1. Network / ISP
        if (ipObj.has("network") && ipObj.get("network").isJsonObject()) {
            JsonObject network = ipObj.getAsJsonObject("network");
            if (ProxyCheck.has(network, "asn")) builder.asn(network.get("asn").getAsString());
            if (ProxyCheck.has(network, "provider")) builder.isp(network.get("provider").getAsString());
        }

        // 2. Location
        if (ipObj.has("location") && ipObj.get("location").isJsonObject()) {
            JsonObject loc = ipObj.getAsJsonObject("location");
            if (ProxyCheck.has(loc, "country_name")) builder.country(loc.get("country_name").getAsString());
        }

        // 3. Detections
        boolean proxyFlag = false;
        if (ipObj.has("detections") && ipObj.get("detections").isJsonObject()) {
            JsonObject det = ipObj.getAsJsonObject("detections");

            boolean isProxy = ProxyCheck.has(det, "proxy") && det.get("proxy").getAsBoolean();
            boolean isVpn = ProxyCheck.has(det, "vpn") && det.get("vpn").getAsBoolean();
            boolean isTor = ProxyCheck.has(det, "tor") && det.get("tor").getAsBoolean();
            boolean isScraper = ProxyCheck.has(det, "scraper") && det.get("scraper").getAsBoolean();
            boolean isCompromised = ProxyCheck.has(det, "compromised") && det.get("compromised").getAsBoolean();
            boolean isHosting = ProxyCheck.has(det, "hosting") && det.get("hosting").getAsBoolean();

            proxyFlag = isProxy || isVpn || isTor || isScraper || isCompromised || isHosting;

            builder.proxy(proxyFlag)
                    .isVpn(isVpn)
                    .isTor(isTor)
                    .isScraper(isScraper)
                    .isCompromised(isCompromised)
                    .isHosting(isHosting);

            if (ProxyCheck.has(det, "risk")) builder.riskScore(det.get("risk").getAsInt());
            if (ProxyCheck.has(det, "confidence")) builder.confidenceScore(det.get("confidence").getAsInt());

            // Primary ProxyType assignment
            ProxyType type = ProxyType.NOT_A_PROXY;
            if (isTor) type = ProxyType.TOR;
            else if (isScraper) type = ProxyType.SCRAPER;
            else if (isCompromised) type = ProxyType.COMPROMISED;
            else if (isVpn) type = ProxyType.VPN;
            else if (isHosting) type = ProxyType.DATACENTER;
            else if (isProxy) type = ProxyType.PUBLIC_PROXY;

            builder.proxyType(type);
        }

        // 4. Attack History Count
        if (ipObj.has("attack_history") && ipObj.get("attack_history").isJsonObject()) {
            //JsonObject attacks = ipObj.getAsJsonObject("attack_history");
            builder.hasAttackHistory(true);
        }

        IPInfo info = builder.build();
        return proxyFlag ? CheckResultHolder.proxy(info) : CheckResultHolder.nonProxy(info);
    }
}