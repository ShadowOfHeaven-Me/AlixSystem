package alix.common.antibot.epoll.syn.signature;

import alix.common.antibot.epoll.syn.analysis.MTUEnvironment;
import alix.common.antibot.epoll.syn.analysis.OS;

public final class SynSignature {

    public final int ipVersion;
    public final int ttl;
    public final int initialTtl;
    public final int distance;       // Calculated network hops away
    public final int ipOptLen;       // Total length of IP options
    public final boolean df;          // Don't Fragment flag (IPv4)
    public final boolean ipIdZero;    // Is the IP ID field zeroed out?
    public final int windowSize;
    public final int mss;
    public final int windowScale;
    public final boolean sackPermitted;
    public final boolean hasTimestamp;
    public final long tsVal;
    public final String optionsLayout;
    public final String p0fSignature; // The final normalized string for DB lookups
    public final OS os;
    public final MTUEnvironment mtuEnv;

    public SynSignature(int ipVersion, int ttl, int initialTtl, int distance, int ipOptLen, boolean df, boolean ipIdZero, int windowSize,
                        int mss, int windowScale, boolean sackPermitted, boolean hasTimestamp, long tsVal, String optionsLayout,
                        String p0fSignature, OS os, MTUEnvironment mtuEnv) {
        this.ipVersion = ipVersion;
        this.ttl = ttl;
        this.initialTtl = initialTtl;
        this.distance = distance;
        this.ipOptLen = ipOptLen;
        this.df = df;
        this.ipIdZero = ipIdZero;
        this.windowSize = windowSize;
        this.mss = mss;
        this.windowScale = windowScale;
        this.sackPermitted = sackPermitted;
        this.hasTimestamp = hasTimestamp;
        this.tsVal = tsVal;
        this.optionsLayout = optionsLayout;
        this.p0fSignature = p0fSignature;
        this.os = os;
        this.mtuEnv = mtuEnv;
    }
}
