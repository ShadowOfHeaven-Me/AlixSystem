package shadow.systems.login.filters;

import alix.common.messages.Messages;
import shadow.systems.login.vpn.ProxyCheckManager;


public final class AntiVPN implements ConnectionFilter {

    public static final AntiVPN INSTANCE = new AntiVPN();
    private final String s = Messages.get("anti-vpn");
    private final ProxyCheckManager proxyCheck = ProxyCheckManager.INSTANCE;

    private AntiVPN() {
    }

    @Override
    public boolean disallowJoin(String address, String name) {
        return this.proxyCheck.isProxy(address);
    }

    /*try {
            JsonElement out = HttpsHandler.readURL("https://proxycheck.io/v2/" + address + "?vpn=1&asn=1");
            String proxy = out.getAsJsonObject().get(address).getAsJsonObject().get("proxy").getAsString();
            return proxy.equals("yes");
        } catch (Exception e) {
            //e.printStackTrace(); //<- no internet, no element, etc.
            return false;//JavaUtils.isPluginLanguageEnglish ? "Something went wrong. Please try reconnecting."
        }*/

    @Override
    public String getReason() {
        return s;
    }
}