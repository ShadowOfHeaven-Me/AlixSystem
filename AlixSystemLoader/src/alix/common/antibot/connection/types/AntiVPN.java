package alix.common.antibot.connection.types;

import alix.common.utils.i18n.HttpsHandler;
import alix.common.antibot.connection.ConnectionFilter;
import alix.common.messages.Messages;
import com.google.gson.JsonElement;


public class AntiVPN implements ConnectionFilter {

    private final String s = Messages.get("anti-vpn");

    @Override
    public boolean disallowJoin(String address, String name) {
        try {
            JsonElement out = HttpsHandler.readURL("https://proxycheck.io/v2/" + address + "?vpn=1&asn=1");
            String proxy = out.getAsJsonObject().get(address).getAsJsonObject().get("proxy").getAsString();
            return proxy.equals("yes");
        } catch (Exception e) {
            //e.printStackTrace(); //<- no internet, no element, etc.
            return false;//JavaUtils.isPluginLanguageEnglish ? "Something went wrong. Please try reconnecting."
        }
    }

    @Override
    public String getReason() {
        return s;
    }
}