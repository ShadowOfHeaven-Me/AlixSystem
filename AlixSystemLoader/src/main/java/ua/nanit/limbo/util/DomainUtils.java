package ua.nanit.limbo.util;

import alix.common.utils.other.throwable.AlixException;

import java.util.regex.Pattern;

public final class DomainUtils {

    private static final Pattern SPECIAL_CASES = Pattern.compile(
            "^(localhost|0)$",
            Pattern.CASE_INSENSITIVE
    );

    // 2. Standard IPv4 validation (0.0.0.0 to 255.255.255.255)
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    // 3. Domain validation enforcing RFC 1035/1123 rules per label:
    // - Must contain at least one dot separator
    // - Labels cannot start or end with a hyphen
    // - Cannot end with a trailing dot
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$"
    );

    public static boolean isValidDomainOrIp(String input) {
        if (input == null)
            return false;

        String trimmed = input.trim();

        if (trimmed.isEmpty())
            return false;

        return DOMAIN_PATTERN.matcher(trimmed).matches() ||
               IPV4_PATTERN.matcher(trimmed).matches() ||
               SPECIAL_CASES.matcher(trimmed).matches();
    }

    public static String extractHost(String host) {
        var result = extractHost0(host);
        //invalid domain attack?
        if (!isValidDomainOrIp(result))
            throw new AlixException("Invalid host: '" + host + "'");
        return result;
    }

    //per: https://github.com/MinecraftForge/MinecraftForge/blob/478d8b3f181220c9be6911311ed2177d07febc08/src/main/java/net/minecraftforge/network/NetworkContext.java#L91
    //extra info can be sent from bedrock/forge players in the host name
    private static String extractHost0(String host) {
        int idx = host.indexOf('\0');
        var f = idx != -1 ? host.substring(0, idx) : host;
        //this dot comes from the PlayIt tunnel (maybe?)
        return f.charAt(f.length() - 1) == '.' ? f.substring(0, f.length() - 1) : f;
    }
}