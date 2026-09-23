package alix.common.connection.vpn;

import alix.common.utils.i18n.HttpsHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface ProxyCheck {

    CheckResultHolder isProxy(String address);

    static JsonElement getResponse(String urlLink) {
        return HttpsHandler.getResponse(urlLink);
    }

    /**
     * @return true if {@code key} is present in {@code obj} AND its value isn't JSON null. {@link
     * JsonObject#has} alone only checks presence - a third-party API returning an explicit null for a
     * field it doesn't have data for on some IPs (common: country/ISP/ASN lookups) still counts as
     * "present" to has(), and calling e.g. getAsString() on that null value throws
     * UnsupportedOperationException rather than returning null.
     */
    static boolean has(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull();
    }
}