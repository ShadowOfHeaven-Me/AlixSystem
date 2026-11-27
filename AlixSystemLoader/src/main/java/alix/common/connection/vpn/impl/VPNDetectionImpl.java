package alix.common.connection.vpn.impl;

import alix.common.connection.vpn.CheckResultHolder;
import alix.common.connection.vpn.IPInfo;
import alix.common.connection.vpn.ProxyCheck;
import alix.common.connection.vpn.ProxyType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class VPNDetectionImpl implements ProxyCheck {

    // No key and no daily cap (https://vpndetection.io/terms#no-key), so no rate limiter here
    @Override
    public CheckResultHolder isProxy(String address) {
        JsonElement out = ProxyCheck.getResponse("https://api.vpndetection.io/" + address);
        if (out == null || !out.isJsonObject()) return CheckResultHolder.UNAVAILABLE;

        JsonObject root = out.getAsJsonObject();
        if (!ProxyCheck.has(root, "is_vpn")) return CheckResultHolder.UNAVAILABLE;

        IPInfo.Builder builder = new IPInfo.Builder(address, "vpndetection.io");

        boolean isVpn = root.get("is_vpn").getAsBoolean();
        boolean isTor = ProxyCheck.has(root, "is_tor") && root.get("is_tor").getAsBoolean();
        // Privacy relays such as iCloud Private Relay
        boolean isRelay = ProxyCheck.has(root, "is_relay") && root.get("is_relay").getAsBoolean();
        boolean isHosting = false;

        if (root.has("asn") && root.get("asn").isJsonObject()) {
            JsonObject asnObj = root.getAsJsonObject("asn");
            if (ProxyCheck.has(asnObj, "asn")) builder.asn("AS" + asnObj.get("asn").getAsInt());
            if (ProxyCheck.has(asnObj, "name")) builder.isp(asnObj.get("name").getAsString());
            isHosting = ProxyCheck.has(asnObj, "type") && "hosting".equals(asnObj.get("type").getAsString());
        }

        if (root.has("location") && root.get("location").isJsonObject()) {
            JsonObject loc = root.getAsJsonObject("location");
            if (ProxyCheck.has(loc, "country")) builder.country(loc.get("country").getAsString());
        }

        boolean isProxyFlag = isVpn || isTor || isRelay || isHosting;

        builder.proxy(isProxyFlag)
                .isTor(isTor)
                .isVpn(isVpn)
                .isHosting(isHosting);

        ProxyType type = ProxyType.NOT_A_PROXY;
        if (isTor) type = ProxyType.TOR;
        else if (isVpn) type = ProxyType.VPN;
        else if (isHosting) type = ProxyType.DATACENTER;
        else if (isRelay) type = ProxyType.PROXY;
        builder.proxyType(type);

        IPInfo info = builder.build();
        return isProxyFlag ? CheckResultHolder.proxy(info) : CheckResultHolder.nonProxy(info);
    }
}
