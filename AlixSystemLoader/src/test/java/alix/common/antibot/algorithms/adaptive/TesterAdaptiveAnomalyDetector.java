package alix.common.antibot.algorithms.adaptive;

import alix.common.connection.ratelimit.RateLimiter;

import java.net.InetAddress;
import java.util.Random;

//Standalone main()-driven driver (same convention as TesterBCrypt/TesterCompressor - a plain script run
//manually, not JUnit) sanity-checking AdaptiveAnomalyDetector's tuning against synthetic traffic shapes,
//including the fingerprint-suspicion connection weighting (see LimboIntegration#connectionWeight).
//
//Drives SourceMetrics directly via forceBucketForTest(), which reuses the real bucket-processing logic
//(cold-start heuristic, CUSUM, EWMA) but skips the bucketMillis wall-clock wait. Deliberately avoids the
//real AdaptiveAnomalyDetector class - referencing it triggers a static initializer that schedules
//AlixScheduler tasks needing a live Bukkit/Velocity platform this test source set doesn't have.
//newPerIpMetrics()/newSubnetMetrics() below are a copy of AdaptiveAnomalyDetector's factory-method
//constants - keep them in sync if those change.
public final class TesterAdaptiveAnomalyDetector {

    public static void main(String[] args) {
        System.out.println("=== Scenario 1: ordinary players, per-IP (each IP connects once, occasionally) ===");
        benignPerIpTraffic();

        System.out.println();
        System.out.println("=== Scenario 2: LAN party / school subnet, many distinct benign IPs joining together ===");
        benignSubnetBurst();

        System.out.println();
        System.out.println("=== Scenario 3: single-IP flood, UNWEIGHTED (no/clean fingerprint) ===");
        maliciousPerIpFlood(1);

        System.out.println();
        System.out.println("=== Scenario 4: single-IP flood, WEIGHTED (suspicious MTU+OS fingerprint, weight=3) ===");
        maliciousPerIpFlood(3);
    }

    //A handful of distinct residential IPs each reconnecting a couple times over a play session - most
    //buckets for any single IP's SourceMetrics are empty. Expect NORMAL the entire way through: a real
    //player occasionally joining must never get flagged.
    private static void benignPerIpTraffic() {
        Random random = new Random(1);
        for (int ip = 0; ip < 5; ip++) {
            SourceMetrics<InetAddress> metrics = newPerIpMetrics();
            State worst = State.NORMAL;
            for (int bucket = 0; bucket < 40; bucket++) {
                //an ordinary player connects at most once in a rare while - mostly 0, occasionally 1
                long count = random.nextInt(12) == 0 ? 1 : 0;
                State s = metrics.forceBucketForTest(count, 0);
                worst = worst.worst(s);
            }
            System.out.println("  IP #" + ip + " worst state over 40 buckets: " + worst);
        }
    }

    //~150 genuinely distinct IPs behind the same /24 (e.g. a school's NAT) all joining within a short
    //window - real spread-out client diversity, not one source hammering. Expect at most ELEVATED, never
    //an outright ATTACK verdict for this shape.
    private static void benignSubnetBurst() {
        SourceMetrics<Integer> subnet = newSubnetMetrics();
        State worst = State.NORMAL;
        //ramps up and back down over a few buckets like real people arriving, not an instant spike
        int[] joinsPerBucket = {5, 15, 30, 40, 25, 15, 8, 4, 2, 1, 1, 1, 0, 0, 0};
        for (int count : joinsPerBucket) {
            State s = subnet.forceBucketForTest(count, 0);
            worst = worst.worst(s);
            System.out.println("  bucket join count=" + count + " -> state=" + s);
        }
        System.out.println("  worst state: " + worst);
    }

    //One IP opening connections far faster than any real player would, either with an unremarkable
    //fingerprint (weight 1, as if no SynSignature was available or nothing about it looked suspicious) or a
    //suspicious one (weight 3, as if TelemetryProfiler.synSignature(channel) came back with a maxed-out
    //MTU+OS suspicion score) - shows how many buckets/real connections it takes each to trip ATTACK.
    private static void maliciousPerIpFlood(int weight) {
        SourceMetrics<InetAddress> metrics = newPerIpMetrics();
        int connectionsPerBucket = 6; //well above any real single player's rate
        for (int bucket = 0; bucket < 10; bucket++) {
            long weightedCount = (long) connectionsPerBucket * weight;
            State s = metrics.forceBucketForTest(weightedCount, 0);
            System.out.println("  weight=" + weight + " bucket #" + bucket
                    + " raw_connections=" + connectionsPerBucket + " weighted_count=" + weightedCount
                    + " -> state=" + s);
            if (s == State.ATTACK) {
                System.out.println("  -> tripped ATTACK after " + (bucket + 1) + " buckets ("
                        + ((bucket + 1) * connectionsPerBucket) + " real connections)");
                return;
            }
        }
        System.out.println("  -> never reached ATTACK within 10 buckets");
    }

    //Copy of AdaptiveAnomalyDetector#newPerIpMetrics() - see this class's header comment for why it can't
    //just call through to the real one.
    private static SourceMetrics<InetAddress> newPerIpMetrics() {
        return new SourceMetrics<>(0.1, 30, 1.5, 8, 15, 4_000,
                2, 3, 8, 20,
                new RateLimiter<>(3, 10));
    }

    //Copy of AdaptiveAnomalyDetector#newSubnetMetrics() - see this class's header comment for why it can't
    //just call through to the real one.
    private static <T> SourceMetrics<T> newSubnetMetrics() {
        return new SourceMetrics<>(0.05, 60, 2.0, 10, 18, 15_000,
                3, 7, 30, 200,
                new RateLimiter<>(10, 10));
    }
}
