package ua.nanit.limbo.util.domain;

import alix.common.utils.AlixCache;
import com.google.common.net.InetAddresses;

import java.util.Map;
import java.util.regex.Pattern;

public final class DomainUtils {

    private static final Pattern SPECIAL_CASES = Pattern.compile(
            "^(localhost|0)$",
            Pattern.CASE_INSENSITIVE
    );

    // Standard IPv4 validation (0.0.0.0 to 255.255.255.255)
    /*private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );*/

    // Domain validation enforcing RFC 1035/1123 rules per label:
    // - Must contain at least one dot separator
    // - Labels cannot start or end with a hyphen
    // - Cannot end with a trailing dot
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$"
    );

    private static final Map<String, Boolean> VALIDITY_CACHE = AlixCache.newBuilder().maximumSize(512).<String, Boolean>build().asMap();
    private static final Map<String, String> EXTRACTION_CACHE = AlixCache.newBuilder().maximumSize(512).<String, String>build().asMap();

    public static boolean isValidDomainOrIp(String input) {
        Boolean cache = VALIDITY_CACHE.get(input);
        if (cache != null)
            return cache;

        String host = input.trim();
        if (host.isEmpty())
            return false;

        boolean isValid = DOMAIN_PATTERN.matcher(host).matches() ||
                //IPV4_PATTERN.matcher(host).matches() ||
                InetAddresses.isInetAddress(host) ||
                SPECIAL_CASES.matcher(host).matches();
        VALIDITY_CACHE.put(input, isValid);

        return isValid;
    }

    public static String extractHost(String host) {
        return EXTRACTION_CACHE.computeIfAbsent(host, DomainUtils::computeHost0);
    }

    public static final String INVALID_HOST = "";

    private static String computeHost0(String host) {
        var result = extractHost0(host);
        //invalid domain attack?
        if (!isValidDomainOrIp(result))
            return INVALID_HOST;
        //Log.warning("Invalid extracted host '" + host + "'. Report this immediately!");

        //throw NettySafetyException
        return result;
    }

    //per: https://github.com/MinecraftForge/MinecraftForge/blob/478d8b3f181220c9be6911311ed2177d07febc08/src/main/java/net/minecraftforge/network/NetworkContext.java#L91
    //extra info can be sent from bedrock/forge players in the host name
    static String extractHost0(String host) {
        if (host.isEmpty())
            return host;

        int idx_null = host.indexOf('\0');
        var f = idx_null != -1 ? host.substring(0, idx_null) : host;
        //handle the FQDN dot
        var out = f.charAt(f.length() - 1) == '.' ? f.substring(0, f.length() - 1) : f;

        //ipv6
        if (out.startsWith("["))
            return out;//can these also be with [ipv6]:port?

        //port semicolon
        int idx_semicolon = out.indexOf(':');
        return idx_semicolon != -1 ? out.substring(0, idx_semicolon) : out;
    }
}