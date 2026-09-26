package alix.common.antibot.epoll.syn.analysis;

import lombok.Getter;

@Getter
public enum MTUEnvironment {
    STANDARD_ETHERNET("Ethernet (Standard)", 0),
    DSL_PPPOE("DSL (PPPoE)", 0),

    //Cellular/mobile carriers routinely advertise a reduced effective MTU on their own PDP context/APN
    //framing - not a VPN or tunnel. See MtuAnalyser's docs for the carrier-reported values this range uses.
    CELLULAR_CARRIER("Cellular/Mobile Carrier", 0),

    OLD_ETHERNET("Ethernet (Old)", 25),
    GENERIC_VPN("Generic VPN", 45),
    GENERIC_TUNNEL_OR_VPN("Generic tunnel or VPN", 45),
    UNKNOWN_LOW_MTU("Unknown (Low - Possible Tunnel/Proxy)", 50),

    GCP_INTERNAL("Google Cloud Platform", 75),
    JUMBO_ETHERNET("Jumbo Ethernet (Datacenter)", 85),
    UNKNOWN_HIGH_MTU("Unknown (High - Datacenter/Spoofed)", 90),

    //v2026-09-24: no longer doubles as an "IPSec" signature too - see MtuAnalyser's docs for why lumping
    //IPsec in with GRE's exact byte count was wrong (IPsec's overhead is variable, 50-70 bytes depending on
    //cipher/padding, and essentially never lands on GRE's fixed 1476 by coincidence).
    GRE_TUNNEL("GRE Tunnel", 95),
    IPIP_OR_SIT("IPIP or SIT Tunnel", 95),
    PPTP("PPTP Legacy VPN", 95),
    GIF_IPV6_TUNNEL("GIF (IPv6 tunnel)", 100),

    UNKNOWN_STANDARD_RANGE("Unknown (Standard Range)", 45);

    private final String readableName;
    private final int suspicionScore;

    MTUEnvironment(String readableName, int suspicionScore) {
        this.readableName = readableName;
        this.suspicionScore = suspicionScore;
    }

    public boolean isProxied() {
        return switch (this) {
            case STANDARD_ETHERNET, OLD_ETHERNET, DSL_PPPOE, CELLULAR_CARRIER -> false;
            default -> true;
        };
    }
}
