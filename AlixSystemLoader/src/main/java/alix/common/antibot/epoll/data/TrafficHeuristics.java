package alix.common.antibot.epoll.data;


import alix.common.antibot.epoll.ConnectionStats;
import alix.common.antibot.epoll.TelemetryProfilerImpl;
import alix.common.antibot.epoll.syn.signature.SynSignature;

final class TrafficHeuristics {

    enum ThreatProfile {
        LEGITIMATE,
        LIKELY_LEGITIMATE,
        DATACENTER_PROXY,
        RAW_BOTNET,
        SUSPICIOUS,
        UNKNOWN
    }

    enum Confidence { LOW, MEDIUM, HIGH }

    record Result(
            ThreatProfile profile,
            Confidence confidence,
            int suspicionScore,
            int legitScore,
            String reason
    ) {}

    static Result evaluate(TelemetryProfilerImpl.ConnectionRecord record, ConnectionStats stats) {
        if (record.synSignature == null) {
            return new Result(ThreatProfile.UNKNOWN, Confidence.LOW, 1, 0, "Missing SYN signature");
        }

        int sampleCount = record.tcpSamples.size();
        long l7DeltaMs = record.getL7DeltaT();
        boolean sawMcState = record.nextState == 1 || record.nextState == 2;
        boolean sawL7 = l7DeltaMs >= 0;

        // Grace period / Dead connection handler
        if (sampleCount < 2) {
            if (sawMcState || sawL7) {
                return new Result(ThreatProfile.UNKNOWN, Confidence.LOW, 0, 2, "L7 seen, awaiting sufficient TCP data");
            }
            if (record.getAgeMs() < 750) {
                return new Result(ThreatProfile.UNKNOWN, Confidence.LOW, 0, 0, "Too early, awaiting data");
            }
            return new Result(ThreatProfile.RAW_BOTNET, Confidence.LOW, 5, 0, "Dead connection (No TCP stats, No L7)");
        }

        SynSignature syn = record.synSignature;
        int suspicion = 0;
        int legit = 0;

        // --- 1. OS & MSS Fingerprinting ---
        boolean isWindowsTtl = syn.ttl > 100 && syn.ttl <= 128;
        boolean isLinuxTtl = syn.ttl > 40 && syn.ttl <= 64;
        boolean oddTtl = syn.ttl < 30 || syn.ttl > 150;

        boolean isDatacenterMss = syn.mss == 1460 || syn.mss >= 8000;
        boolean isHomeMss = syn.mss > 1300 && syn.mss < 1460;

        // Using minRtt reveals the true, un-congested distance to the TCP termination point.
        long pingDelta = sawL7 ? Math.abs(l7DeltaMs - stats.minRtt) : -1;
        boolean isProxied = sawL7 && stats.minRtt <= 10 && pingDelta > 100;
        boolean isNaturalPing = sawL7 && pingDelta <= 80;

        // A perfectly sterile connection is a massive red flag for consumer traffic.
        boolean isSterileNetwork = stats.rttStdDev < 2.0 && stats.maxRetransmitBurst == 0 && stats.maxRtt < 10;

        // Natural jitter means they are likely on residential Wi-Fi/4G.
        boolean hasNaturalJitter = stats.rttStdDev > 2.0 && stats.rttStdDev < 50.0;

        // Chaotic connections drop packets in clumps (floods) or swing wildly in ping.
        boolean chaoticTcp = stats.maxRetransmitBurst > 5 || stats.rttStdDev > 150.0;

        // Choking connections hold unacknowledged packets for too long.
        boolean choking = stats.avgUnacked > 5 && stats.durationMs > 2000;

        boolean failedL7 = l7DeltaMs > 4000;

        // --- Scoring Logic ---
        if (sawMcState) legit += 3;
        if (isWindowsTtl) legit += 2;
        if (isHomeMss) legit += 2;
        if (isNaturalPing) legit += 3;
        // Reward natural imperfection, not sterile datacenter perfection.
        if (hasNaturalJitter && stats.maxRetransmitBurst <= 2) legit += 2;

        if (oddTtl) suspicion += 3;
        if (chaoticTcp) suspicion += 4;
        if (choking) suspicion += 2;
        if (failedL7) suspicion += 4;
        if (isProxied) suspicion += 5;
        if (isSterileNetwork) suspicion += 2;
        if (isLinuxTtl && isDatacenterMss) suspicion += 3;

        if ((chaoticTcp || choking) && failedL7) {
            return new Result(ThreatProfile.RAW_BOTNET, Confidence.HIGH, suspicion, legit, "High TCP chaos/choking with L7 failure");
        }

        if (isProxied && (isLinuxTtl || isSterileNetwork) && isDatacenterMss) {
            return new Result(ThreatProfile.DATACENTER_PROXY, Confidence.HIGH, suspicion, legit, "Datacenter fingerprint with sterile network and proxy latency");
        }

        if (sawMcState && isWindowsTtl && isNaturalPing && suspicion <= 2) {
            return new Result(ThreatProfile.LEGITIMATE, Confidence.HIGH, suspicion, legit, "Windows Gamer Fingerprint with natural timing");
        }

        if (sawMcState && legit >= 6 && suspicion <= 3) {
            return new Result(ThreatProfile.LIKELY_LEGITIMATE, Confidence.MEDIUM, suspicion, legit, "Standard flow with minor anomalies");
        }

        // 4. Fallbacks
        if (suspicion >= 6) {
            return new Result(ThreatProfile.SUSPICIOUS, Confidence.MEDIUM, suspicion, legit, "High cumulative anomalous scores");
        }

        return new Result(ThreatProfile.UNKNOWN, Confidence.LOW, suspicion, legit, "Mixed or insufficient statistical data");
    }
}