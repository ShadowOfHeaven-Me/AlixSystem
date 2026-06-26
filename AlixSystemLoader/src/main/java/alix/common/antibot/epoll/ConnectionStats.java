package alix.common.antibot.epoll;

import java.util.List;

public class ConnectionStats {
    // RTT Metrics
    public long minRtt = Long.MAX_VALUE;
    public long maxRtt = -1;
    public double avgRtt = 0;
    public double rttStdDev = 0; // Jitter

    // Unacked Metrics (Congestion)
    public long maxUnacked = 0;
    public double avgUnacked = 0;

    // Retransmission Metrics
    public long totalRetransmits = 0;
    public long maxRetransmitBurst = 0; // Highest drops between two 50ms polls

    // Timing Metrics
    public long durationMs = 0;

    public ConnectionStats(List<TelemetryProfilerImpl.TcpSample> samples) {
        if (samples == null || samples.isEmpty()) return;

        int count = samples.size();
        long rttSum = 0;
        long unackedSum = 0;

        TelemetryProfilerImpl.TcpSample sample0 = samples.get(0);
        long previousRetrans = sample0.totalRetrans();

        for (TelemetryProfilerImpl.TcpSample sample : samples) {
            long rttMs = sample.rtt() / 1000;

            // RTT Accumulation
            if (rttMs < minRtt) minRtt = rttMs;
            if (rttMs > maxRtt) maxRtt = rttMs;
            rttSum += rttMs;

            // Unacked Accumulation
            if (sample.unacked() > maxUnacked) maxUnacked = sample.unacked();
            unackedSum += sample.unacked();

            // Retransmit Burst Calculation
            long retransBurst = sample.totalRetrans() - previousRetrans;
            if (retransBurst > maxRetransmitBurst) maxRetransmitBurst = retransBurst;
            previousRetrans = sample.totalRetrans();
        }

        // Averages
        this.avgRtt = (double) rttSum / count;
        this.avgUnacked = (double) unackedSum / count;

        // Total Retransmits over the observed window
        this.totalRetransmits = samples.get(count - 1).totalRetrans() - sample0.totalRetrans();

        // Duration
        this.durationMs = samples.get(count - 1).timestamp() - sample0.timestamp();

        // Standard Deviation of RTT (Measures network stability/jitter)
        double varianceSum = 0;
        for (TelemetryProfilerImpl.TcpSample sample : samples) {
            double diff = (sample.rtt() / 1000.0) - avgRtt;
            varianceSum += diff * diff;
        }
        this.rttStdDev = Math.sqrt(varianceSum / count);
    }
}