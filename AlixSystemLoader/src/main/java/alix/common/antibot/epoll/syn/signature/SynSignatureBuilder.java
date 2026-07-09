package alix.common.antibot.epoll.syn.signature;

import alix.common.antibot.epoll.syn.analysis.OSFingerprinter;
import alix.common.antibot.epoll.syn.analysis.MtuAnalyser;

public final class SynSignatureBuilder {

    public int ipVersion;
    public int ttl;
    public int initialTtl;
    public int distance;       // Calculated network hops away
    public int ipOptLen;       // Total length of IP options
    public boolean df;          // Don't Fragment flag (IPv4)
    public boolean ipIdZero;    // Is the IP ID field zeroed out?
    public int windowSize;
    public int mss = -1;
    public int windowScale = -1;
    public boolean sackPermitted;
    public boolean hasTimestamp;
    public long tsVal;
    public String optionsLayout;
    public String p0fSignature; // The final normalized string for DB lookups

    public SynSignature build() {
        return new SynSignature(ipVersion, ttl, initialTtl, distance, ipOptLen, df, ipIdZero, windowSize, mss, windowScale, sackPermitted,
                hasTimestamp, tsVal, optionsLayout, p0fSignature, OSFingerprinter.identifyOS(this), MtuAnalyser.guessMtuEnvironment(mss, ipVersion == 6));
    }
}