package alix.common.antibot.epoll.syn.analysis;

import alix.common.utils.config.ConfigParams;

//Recognized MTU values are derived from named, individually-cited overhead constants (IETF RFCs for
//tunneling protocols, real-world carrier data for cellular) rather than bare magic numbers.
public final class MtuAnalyser {

    private static final int STANDARD_ETHERNET_MTU = 1500;
    private static final int JUMBO_ETHERNET_MTU = 9000;

    //RFC 2516 §2.1 (PPPoE): 6-byte PPPoE header + 2-byte PPP protocol-ID field.
    private static final int PPPOE_OVERHEAD = 8;

    //RFC 2784 (GRE): 20-byte outer IPv4 header + GRE's 4-byte header.
    private static final int GRE_OVERHEAD = 24;

    //RFC 2637 (PPTP): 20 (outer IP) + 12 (PPTP's GRE variant, which carries extra Key/Sequence/Ack fields
    //the plain GRE header lacks) + 4 (PPP header for the encapsulated frame).
    private static final int PPTP_OVERHEAD = 36;

    //RFC 2003 (IP-in-IP) / RFC 4213 (SIT, "6in4"): one extra IPv4 header, 20 bytes. GIF shares this math but
    //has no exact-match constant of its own (see the note further down on why there's no IPv4-side GIF case).
    private static final int IPIP_OR_SIT_OVERHEAD = 20;

    //WireGuard: 8-byte UDP header + fixed 32-byte data-message header/tag, on top of the outer IP header.
    //The two values differ only in the OUTER transport, independent of whether THIS connection is IPv4/IPv6
    //(a WireGuard tunnel's endpoint family has no fixed relationship to the traffic it carries) - both are
    //checked in both branches below.
    private static final int WIREGUARD_OVER_IPV4_OVERHEAD = 60; //20 (outer IPv4) + 8 (UDP) + 32 (WG header/tag)
    private static final int WIREGUARD_OVER_IPV6_OVERHEAD = 80; //40 (outer IPv6) + 8 (UDP) + 32 (WG header/tag)

    //RFC 8200's mandatory IPv6 minimum-MTU floor; also where a GIF/6in4 IPv6 tunnel commonly bottoms out.
    private static final int IPV6_MINIMUM_MTU = 1280;

    //Not a tunnel/VPN/proxy - cellular carriers routinely advertise a reduced MTU on their own PDP
    //context/APN framing. No single RFC value exists (carrier-reported: Verizon/AT&T ~1420-1430, LTE ~1428,
    //some MVNOs/roaming lower, e.g. FirstNet 1342), so this is a range, not an exact match. Gated behind
    //ConfigParams#supportMobileConnections (config.yml 'support-mobile-connections', default false) since
    //most servers don't expect mobile players and this range overlaps a WireGuard-over-IPv6-outer tunnel
    //(see guessMtuEnvironment() below) - see MTUEnvironment#isProxied().
    private static final int CELLULAR_MTU_LOW = 1300;
    private static final int CELLULAR_MTU_HIGH = 1432;

    public static MTUEnvironment guessMtuEnvironment(int mss, boolean ipv6) {
        int tcpHeader = 20;
        int ipHeader = ipv6 ? 40 : 20;
        int mtu = mss + ipHeader + tcpHeader;

        // 1. Global Standards
        if (mtu == STANDARD_ETHERNET_MTU) {
            return MTUEnvironment.STANDARD_ETHERNET;
        }
        if (mtu == JUMBO_ETHERNET_MTU) {
            return MTUEnvironment.JUMBO_ETHERNET;
        }

        // 2. Cellular/mobile carrier - opt-in, see the constants' docs above. Checked before WireGuard: 1500
        // - WIREGUARD_OVER_IPV6_OVERHEAD = 1420, which also falls in this range (a genuine ambiguity between
        // an AT&T-style cellular MTU and a WireGuard-over-IPv6-outer tunnel). Resolved toward the benign
        // explanation only once mobile support is enabled; otherwise it falls through to a more specific
        // signature below (e.g. PPPoE+WireGuard) or a generic unknown-range bucket.
        if (ConfigParams.supportMobileConnections && mtu >= CELLULAR_MTU_LOW && mtu <= CELLULAR_MTU_HIGH) {
            return MTUEnvironment.CELLULAR_CARRIER;
        }

        // 3. WireGuard - checked before the platform-specific branches below since, per the docs on the two
        // constants above, either value can legitimately show up regardless of whether THIS connection is
        // IPv4 or IPv6.
        if (mtu == STANDARD_ETHERNET_MTU - WIREGUARD_OVER_IPV4_OVERHEAD
                || mtu == STANDARD_ETHERNET_MTU - WIREGUARD_OVER_IPV6_OVERHEAD) {
            return MTUEnvironment.GENERIC_VPN;
        }

        //A player can have two independent, stacking causes for a lower MTU (PPPoE + a tunnel on top of it)
        //that a single-overhead exact-match can't recognize. Classified as the tunnel type, not PPPoE, since
        //PPPoE alone is unremarkable and the tunnel underneath is what matters.
        if (mtu == STANDARD_ETHERNET_MTU - PPPOE_OVERHEAD - WIREGUARD_OVER_IPV4_OVERHEAD
                || mtu == STANDARD_ETHERNET_MTU - PPPOE_OVERHEAD - WIREGUARD_OVER_IPV6_OVERHEAD) {
            return MTUEnvironment.GENERIC_VPN;
        }

        // 4. IPv6 Specific Footprints
        if (ipv6) {
            if (mtu == IPV6_MINIMUM_MTU) {
                return MTUEnvironment.GIF_IPV6_TUNNEL;
            }
            if (mtu == STANDARD_ETHERNET_MTU - PPPOE_OVERHEAD) {
                return MTUEnvironment.DSL_PPPOE;
            }
            if (mtu >= 1200 && mtu < STANDARD_ETHERNET_MTU - WIREGUARD_OVER_IPV4_OVERHEAD) {
                return MTUEnvironment.GENERIC_TUNNEL_OR_VPN;
            }
        }
        // 5. IPv4 Specific Footprints
        else {
            switch (mtu) {
                case 576:
                    return MTUEnvironment.OLD_ETHERNET;
            }
            if (mtu == STANDARD_ETHERNET_MTU - PPPOE_OVERHEAD) {
                return MTUEnvironment.DSL_PPPOE;
            }
            if (mtu == STANDARD_ETHERNET_MTU - GRE_OVERHEAD) {
                return MTUEnvironment.GRE_TUNNEL;
            }
            if (mtu == STANDARD_ETHERNET_MTU - IPIP_OR_SIT_OVERHEAD) {
                return MTUEnvironment.IPIP_OR_SIT;
            }
            if (mtu == STANDARD_ETHERNET_MTU - PPTP_OVERHEAD) {
                return MTUEnvironment.PPTP;
            }
            //Combined PPPoE + IPv4-side tunnel overheads, same reasoning as the PPPoE+WireGuard check above.
            if (mtu == STANDARD_ETHERNET_MTU - PPPOE_OVERHEAD - GRE_OVERHEAD) {
                return MTUEnvironment.GRE_TUNNEL;
            }
            if (mtu == STANDARD_ETHERNET_MTU - PPPOE_OVERHEAD - IPIP_OR_SIT_OVERHEAD) {
                return MTUEnvironment.IPIP_OR_SIT;
            }
            if (mtu == STANDARD_ETHERNET_MTU - PPPOE_OVERHEAD - PPTP_OVERHEAD) {
                return MTUEnvironment.PPTP;
            }
            //No IPv4-side GIF/IPv6-tunnel case: a simple IPv6-in-IPv4 tunnel is 20 bytes of overhead (see
            //IPIP_OR_SIT_OVERHEAD), not the old code's unexplained 260-byte guess, and no documented overhead
            //combination lands on it - an unusually low IPv4 MTU falls through to UNKNOWN_LOW_MTU instead.
            if (mtu >= 1300 && mtu <= 1460) {
                return MTUEnvironment.GENERIC_TUNNEL_OR_VPN;
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

    private MtuAnalyser() {
    }
}
