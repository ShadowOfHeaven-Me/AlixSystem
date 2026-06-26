package alix.common.connection.filters;

import alix.common.connection.vpn.ProxyCheckManager;
import alix.common.data.file.AllowListFileManager;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigProvider;
import io.netty.channel.Channel;

import java.net.InetAddress;
import java.util.function.Consumer;


public final class AntiVPN {

    private static final boolean isEnabled = ConfigProvider.config.getBoolean("anti-vpn");
    public static final AntiVPN INSTANCE = new AntiVPN();
    public static final String antiVpnMessage = Messages.get("anti-vpn");
    private final ProxyCheckManager proxyCheck = ProxyCheckManager.INSTANCE;

    private AntiVPN() {
    }

    void disallowJoin0(Channel channel, InetAddress ip, String strAddress, Consumer<Boolean> isProxy) {
        this.proxyCheck.isProxy(channel, ip, strAddress, isProxy);
    }

    public static void disallowJoin(Channel channel, InetAddress ip, String name, Consumer<Boolean> isProxy) {
        if (!isEnabled || AllowListFileManager.has(name)) {
            isProxy.accept(false);
            return;
        }

        INSTANCE.disallowJoin0(channel, ip, ip.getHostAddress(), isProxy);
    }

    /*try {
            JsonElement out = HttpsHandler.readURL("https://proxycheck.io/v2/" + address + "?vpn=1&asn=1");
            String proxy = out.getAsJsonObject().get(address).getAsJsonObject().get("proxy").getAsString();
            return proxy.equals("yes");
        } catch (Exception e) {
            //e.printStackTrace(); //<- no internet, no element, etc.
            return false;//JavaUtils.isPluginLanguageEnglish ? "Something went wrong. Please try reconnecting."
        }*/

    public String getReason() {
        return antiVpnMessage;
    }
}