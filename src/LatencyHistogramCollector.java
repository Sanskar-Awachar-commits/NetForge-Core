public class LatencyHistogramCollector implements Tickable {
    // Upper bounds (inclusive) for buckets; last entry represents latency > 50
    private static final long[] BUCKET_LIMITS = {5, 10, 20, 50, Long.MAX_VALUE};
    private final long[] counts = new long[BUCKET_LIMITS.length];
    private long totalSamples = 0;

    public void tick(long currentTick) {
        // Deterministic discrete-event collection is passive; no-op per tick.
    }

    public void recordLatency(long latencyTicks) {
        if (latencyTicks < 0) {
            latencyTicks = 0;
        }
        for (int i = 0; i < BUCKET_LIMITS.length; i++) {
            if (latencyTicks <= BUCKET_LIMITS[i]) {
                counts[i]++;
                break;
            }
        }
        totalSamples++;
    }

    public double getP50() {
        return calculatePercentile(0.50);
    }

    public double getP95() {
        return calculatePercentile(0.95);
    }

    public double getP99() {
        return calculatePercentile(0.99);
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    private double calculatePercentile(double percentile) {
        if (totalSamples == 0) {
            return 0.0;
        }
        double targetRank = percentile * totalSamples;
        long cumulative = 0;

        for (int i = 0; i < BUCKET_LIMITS.length; i++) {
            long bucketCount = counts[i];
            if (cumulative + bucketCount >= targetRank) {
                long lowerBound = (i == 0) ? 0 : BUCKET_LIMITS[i - 1] + 1;
                long upperBound = (i == BUCKET_LIMITS.length - 1) ? BUCKET_LIMITS[i - 1] * 2 : BUCKET_LIMITS[i];
                
                if (bucketCount == 0) {
                    return lowerBound;
                }
                double fraction = (targetRank - cumulative) / (double) bucketCount;
                return lowerBound + fraction * (upperBound - lowerBound);
            }
            cumulative += bucketCount;
        }
        return BUCKET_LIMITS[BUCKET_LIMITS.length - 2];
    }
}   