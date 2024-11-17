package alix.common.connection.vpn;

import alix.common.utils.i18n.HttpsHandler;
import com.google.gson.JsonElement;

public interface ProxyCheck {

    CheckResult isProxy(String address);

    static JsonElement getResponse(String urlLink) {
        return HttpsHandler.getResponse(urlLink);
    }
}