package alix.common.connection.vpn.serializer;

import alix.common.connection.vpn.IPInfo;
import alix.common.connection.vpn.ProxyType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class IPInfoBinarySerializer {

    // Tri-State encoding for Nullable Booleans
    private static final int TRI_NULL = 0;
    private static final int TRI_FALSE = 1;
    private static final int TRI_TRUE = 2;

    public static void serialize(IPInfo info, DataOutputStream out) throws IOException {
        // 1. Provider ID (1 byte)
        out.writeByte(encodeProvider(info.getProviderName()));

        // 2. Country (2 ASCII bytes)
        String country = info.getCountry();
        if (country != null && country.length() == 2) {
            out.writeByte(country.charAt(0));
            out.writeByte(country.charAt(1));
        } else {
            out.writeByte('?');
            out.writeByte('?');
        }

        // 3. Scores (1 byte each)
        out.writeByte(info.getRiskScore() & 0xFF);
        out.writeByte(info.getConfidenceScore() & 0xFF);

        // 4. ASN (4 bytes)
        out.writeInt(parseAsn(info.getAsn()));

        // 5. Bitmask for Booleans & Enums (4 bytes)
        int flags = 0;
        flags |= (encodeTriState(info.isProxy()) & 0x3);
        flags |= (encodeTriState(info.isVpn()) & 0x3) << 2;
        flags |= (encodeTriState(info.isTor()) & 0x3) << 4;
        flags |= (encodeTriState(info.isScraper()) & 0x3) << 6;
        flags |= (encodeTriState(info.isCompromised()) & 0x3) << 8;
        flags |= (encodeTriState(info.isHosting()) & 0x3) << 10;
        flags |= (info.isHasAttackHistory() ? 1 : 0) << 12;

        int proxyTypeOrdinal = info.getProxyType() != null ? info.getProxyType().ordinal() : ProxyType.PROXY.ordinal();
        flags |= (proxyTypeOrdinal & 0x0F) << 13; // 4 bits for ProxyType enum (up to 16 values)

        out.writeInt(flags);

        // 6. ISP (Length-prefixed string)
        String isp = info.getIsp() != null ? info.getIsp() : "Unknown";
        byte[] ispBytes = isp.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int ispLen = Math.min(ispBytes.length, 255); // Cap at 255 bytes
        out.writeByte(ispLen);
        out.write(ispBytes, 0, ispLen);
    }

    public static IPInfo deserialize(String ipStr, DataInputStream in) throws IOException {
        // 1. Provider
        String provider = decodeProvider(in.readByte() & 0xFF);

        // 2. Country
        char c1 = (char) in.readByte();
        char c2 = (char) in.readByte();
        String country = (c1 == '?' && c2 == '?') ? "Unknown" : "" + c1 + c2;

        // 3. Scores
        int riskScore = in.readByte() & 0xFF;
        int confidenceScore = in.readByte() & 0xFF;

        // 4. ASN
        int rawAsn = in.readInt();
        String asn = rawAsn == -1 ? "Unknown" : "AS" + rawAsn;

        // 5. Bitmask
        int flags = in.readInt();

        Boolean proxy = decodeTriState(flags & 0x3);
        Boolean isVpn = decodeTriState((flags >> 2) & 0x3);
        Boolean isTor = decodeTriState((flags >> 4) & 0x3);
        Boolean isScraper = decodeTriState((flags >> 6) & 0x3);
        Boolean isCompromised = decodeTriState((flags >> 8) & 0x3);
        Boolean isHosting = decodeTriState((flags >> 10) & 0x3);
        boolean hasAttackHistory = ((flags >> 12) & 0x1) == 1;

        int proxyTypeOrdinal = (flags >> 13) & 0x0F;
        ProxyType proxyType = ProxyType.values()[Math.min(proxyTypeOrdinal, ProxyType.values().length - 1)];

        // 6. ISP
        int ispLen = in.readByte() & 0xFF;
        byte[] ispBytes = new byte[ispLen];
        in.readFully(ispBytes);
        String isp = new String(ispBytes, java.nio.charset.StandardCharsets.UTF_8);

        return new IPInfo.Builder(ipStr, provider)
                .country(country)
                .isp(isp)
                .asn(asn)
                .riskScore(riskScore)
                .confidenceScore(confidenceScore)
                .proxy(proxy)
                .isVpn(isVpn)
                .isTor(isTor)
                .isScraper(isScraper)
                .isCompromised(isCompromised)
                .isHosting(isHosting)
                .hasAttackHistory(hasAttackHistory)
                .proxyType(proxyType)
                .build();
    }

    private static int encodeTriState(Boolean b) {
        if (b == null) return TRI_NULL;
        return b ? TRI_TRUE : TRI_FALSE;
    }

    private static Boolean decodeTriState(int val) {
        if (val == TRI_TRUE) return Boolean.TRUE;
        if (val == TRI_FALSE) return Boolean.FALSE;
        return null;
    }

    private static int parseAsn(String asn) {
        if (asn == null) return -1;
        try {
            return Integer.parseInt(asn.replace("AS", "").trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static byte encodeProvider(String provider) {
        if (provider == null) return 0;
        return switch (provider.toLowerCase()) {
            case "ip-api.com" -> 1;
            case "ip2location" -> 2;
            case "proxycheck.io (v3)" -> 3;
            case "kauri (funkemunky)" -> 4;
            case "ipapi.is" -> 5;
            case "vpndetection.io" -> 6;
            default -> 0;
        };
    }

    private static String decodeProvider(int id) {
        return switch (id) {
            case 1 -> "ip-api.com";
            case 2 -> "IP2Location";
            case 3 -> "proxycheck.io (v3)";
            case 4 -> "Kauri (Funkemunky)";
            case 5 -> "ipapi.is";
            case 6 -> "vpndetection.io";
            default -> "Unknown";
        };
    }
}