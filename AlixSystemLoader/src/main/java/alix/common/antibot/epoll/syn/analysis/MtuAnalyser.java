package alix.common.antibot.epoll.syn.analysis;

import alix.common.utils.config.ConfigParams;

//Recognized MTU values are derived from named, individually-cited overhead constants (IETF RFCs for
//tunneling protocols, real-world carrier data for cellular) rather than bare magic numbers.
public final class MtuAnalyser {

    private static final int STANDARD_ETHERNET_MTU = 1500;
    private static final int JUMBO_ETHERNET_MTU = 9000;
    private static final int OLD_ETHERNET_MTU = 576;

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

    //A single overhead constant paired with the environment it signals. Matched against the MTU deficit
    //below the standard 1500 both on its own and with PPPOE_OVERHEAD stacked on top (see matchTunnel()) -
    //two entries in this table, not four hand-written branches, since PPPoE is an independent, optional
    //cause layered underneath whichever tunnel is actually in play.
    private record TunnelSignature(int overhead, MTUEnvironment environment) {
    }

    //WireGuard's endpoint family has no fixed relationship to whether THIS connection is IPv4/IPv6 (see the
    //two overhead constants' docs), so both variants apply on both platforms.
    private static final TunnelSignature[] IPV4_TUNNELS = {
            new TunnelSignature(WIREGUARD_OVER_IPV4_OVERHEAD, MTUEnvironment.GENERIC_VPN),
            new TunnelSignature(WIREGUARD_OVER_IPV6_OVERHEAD, MTUEnvironment.GENERIC_VPN),
            new TunnelSignature(GRE_OVERHEAD, MTUEnvironment.GRE_TUNNEL),
            new TunnelSignature(IPIP_OR_SIT_OVERHEAD, MTUEnvironment.IPIP_OR_SIT),
            new TunnelSignature(PPTP_OVERHEAD, MTUEnvironment.PPTP),
            //No IPv4-side GIF/IPv6-tunnel entry: a simple IPv6-in-IPv4 tunnel is IPIP_OR_SIT_OVERHEAD's 20
            //bytes, not the old code's unexplained 260-byte guess, and no other documented overhead lands on
            //it - an unusually low IPv4 MTU with no match falls through to UNKNOWN_LOW_MTU instead.
    };

    private static final TunnelSignature[] IPV6_TUNNELS = {
            new TunnelSignature(WIREGUARD_OVER_IPV4_OVERHEAD, MTUEnvironment.GENERIC_VPN),
            new TunnelSignature(WIREGUARD_OVER_IPV6_OVERHEAD, MTUEnvironment.GENERIC_VPN),
    };

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
        if (!ipv6 && mtu == OLD_ETHERNET_MTU) {
            return MTUEnvironment.OLD_ETHERNET;
        }

        // 2. Cellular/mobile carrier - opt-in, see the constants' docs above. Checked before the tunnel
        // matching below: 1500 - WIREGUARD_OVER_IPV6_OVERHEAD = 1420, which also falls in this range (a
        // genuine ambiguity between an AT&T-style cellular MTU and a WireGuard-over-IPv6-outer tunnel).
        // Resolved toward the benign explanation only once mobile support is enabled; otherwise it falls
        // through to a more specific tunnel signature or a generic unknown-range bucket.
        if (ConfigParams.supportMobileConnections && mtu >= CELLULAR_MTU_LOW && mtu <= CELLULAR_MTU_HIGH) {
            return MTUEnvironment.CELLULAR_CARRIER;
        }

        if (ipv6 && mtu == IPV6_MINIMUM_MTU) {
            return MTUEnvironment.GIF_IPV6_TUNNEL;
        }

        //Stacked-cause matching: a low MTU can come from any combination of independent causes layered on
        //top of each other (e.g. PPPoE plus a VPN) that a fixed, hand-enumerated list of pairs can't keep up
        //with as new tunnel types get added. matchTunnel() peels PPPoE's overhead off the deficit below the
        //standard MTU - present or not, since PPPoE alone is unremarkable - then matches whatever's left
        //against the known tunnel signatures, so every tunnel composes with PPPoE automatically instead of
        //needing its own "PPPoE + X" branch.
        MTUEnvironment tunnel = matchTunnel(STANDARD_ETHERNET_MTU - mtu, ipv6 ? IPV6_TUNNELS : IPV4_TUNNELS);
        if (tunnel != null) {
            return tunnel;
        }

        // Platform-specific fallback ranges for anything that didn't match a known signature
        if (ipv6) {
            if (mtu >= 1200 && mtu < STANDARD_ETHERNET_MTU - WIREGUARD_OVER_IPV4_OVERHEAD) {
                return MTUEnvironment.GENERIC_TUNNEL_OR_VPN;
            }
        } else {
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

    private static MTUEnvironment matchTunnel(int deficit, TunnelSignature[] tunnels) {
        for (TunnelSignature tunnel : tunnels) {
            if (deficit == tunnel.overhead() || deficit == tunnel.overhead() + PPPOE_OVERHEAD) {
                return tunnel.environment();
            }
        }
        return deficit == PPPOE_OVERHEAD ? MTUEnvironment.DSL_PPPOE : null;
    }

    private MtuAnalyser() {
    }
}
