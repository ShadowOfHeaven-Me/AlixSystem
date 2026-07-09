package alix.common.antibot.epoll.syn.analysis;

import lombok.Getter;

@Getter
public enum MTUEnvironment {
    STANDARD_ETHERNET("Ethernet (Standard)", 0),
    DSL_PPPOE("DSL (PPPoE)", 0),

    OLD_ETHERNET("Ethernet (Old)", 25),
    WIREGUARD_VPN("WireGuard VPN", 45),
    GENERIC_TUNNEL_OR_VPN("Generic tunnel or VPN", 45),
    UNKNOWN_LOW_MTU("Unknown (Low - Possible Tunnel/Proxy)", 50),

    GCP_INTERNAL("Google Cloud Platform", 75),
    JUMBO_ETHERNET("Jumbo Ethernet (Datacenter)", 85),
    UNKNOWN_HIGH_MTU("Unknown (High - Datacenter/Spoofed)", 90),

    IPSEC_OR_GRE("IPSec or GRE Tunnel", 95),
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
            case STANDARD_ETHERNET, OLD_ETHERNET, DSL_PPPOE -> false;
            default -> true;
        };
    }
}