package alix.common.connection.filters;

import alix.common.connection.vpn.ProxyCheckManager;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigProvider;

import java.net.InetAddress;


public final class AntiVPN implements ConnectionFilter {

    private static final boolean isEnabled = ConfigProvider.config.getBoolean("anti-vpn");
    public static final AntiVPN INSTANCE = new AntiVPN();
    public static final String antiVpnMessage = Messages.get("anti-vpn");
    private final ProxyCheckManager proxyCheck = ProxyCheckManager.INSTANCE;

    private AntiVPN() {
    }

    @Override
    public boolean disallowJoin(InetAddress ip, String strAddress, String name) {
        return this.proxyCheck.isProxy(ip, strAddress);
    }

    public static boolean disallowJoin(InetAddress ip) {
        return isEnabled && INSTANCE.disallowJoin(ip, ip.getHostAddress(), null);
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
        return antiVpnMessage;
    }
}