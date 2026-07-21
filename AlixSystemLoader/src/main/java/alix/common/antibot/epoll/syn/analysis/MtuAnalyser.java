package alix.common.antibot.epoll.syn.analysis;

public final class MtuAnalyser {

    public static MTUEnvironment guessMtuEnvironment(int mss, boolean ipv6) {
        int tcpHeader = 20;
        int ipHeader = ipv6 ? 40 : 20;
        int mtu = mss + ipHeader + tcpHeader;

        // 1. Global Standards
        switch (mtu) {
            case 1500:
                return MTUEnvironment.STANDARD_ETHERNET;
            case 9000:
                return MTUEnvironment.JUMBO_ETHERNET;
        }

        // 2. IPv6 Specific Footprints
        if (ipv6) {
            switch (mtu) {
                case 1280:
                    return MTUEnvironment.GIF_IPV6_TUNNEL;
                case 1440:
                    return MTUEnvironment.WIREGUARD_VPN;
                case 1432:
                    return MTUEnvironment.DSL_PPPOE;
                default:
                    if (mtu >= 1200 && mtu < 1440) {
                        return MTUEnvironment.GENERIC_TUNNEL_OR_VPN;
                    }
            }
        }
        // 3. IPv4 Specific Footprints
        else {
            switch (mtu) {
                case 576:
                    return MTUEnvironment.OLD_ETHERNET;
                case 1492:
                case 1452:
                case 1454:
                    return MTUEnvironment.DSL_PPPOE;
                case 1420:
                    return MTUEnvironment.WIREGUARD_VPN;
                case 1476:
                    return MTUEnvironment.IPSEC_OR_GRE;
                case 1480:
                    return MTUEnvironment.IPIP_OR_SIT;
                case 1490:
                    return MTUEnvironment.PPTP;
                case 1240:
                    return MTUEnvironment.GIF_IPV6_TUNNEL;
                default:
                    if (mtu >= 1300 && mtu <= 1460) {
                        return MTUEnvironment.GENERIC_TUNNEL_OR_VPN;
                    }
            }
        }

        // 1508, due to (tho rarely) being able to above this
        if (mtu > 1508) {
            return MTUEnvironment.UNKNOWN_HIGH_MTU; //
        } else if (mtu < 1300) {
            return MTUEnvironment.UNKNOWN_LOW_MTU;  // Aggressive proxy structures / heavy legacy encapsulation
        }

        // Falls strictly between 1300 and 1500 but didn't match known signatures
        return MTUEnvironment.UNKNOWN_STANDARD_RANGE;
    }
}