import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class WredQueuePolicy implements QueuePolicy {
    private final QueuePolicy innerQueue;
    private final Map<Integer, Integer> minThresholds;
    private final Map<Integer, Integer> maxThresholds;
    private final Map<Integer, Double> maxDropProbabilities;
    private final int defaultMinThreshold;
    private final int defaultMaxThreshold;
    private final double defaultMaxDropProbability;

    public WredQueuePolicy(
            QueuePolicy innerQueue,
            Map<Integer, Integer> minThresholds,
            Map<Integer, Integer> maxThresholds,
            Map<Integer, Double> maxDropProbabilities,
            int defaultMinThreshold,
            int defaultMaxThreshold,
            double defaultMaxDropProbability) {
        this.innerQueue = Objects.requireNonNull(innerQueue);
        this.minThresholds = Map.copyOf(minThresholds);
        this.maxThresholds = Map.copyOf(maxThresholds);
        this.maxDropProbabilities = Map.copyOf(maxDropProbabilities);
        this.defaultMinThreshold = defaultMinThreshold;
        this.defaultMaxThreshold = defaultMaxThreshold;
        this.defaultMaxDropProbability = defaultMaxDropProbability;
    }

    @Override
    public boolean enqueue(Packet packet) {
        int fill = innerQueue.currentSize();
        int priority = packet.priority();

        int minThresh = minThresholds.getOrDefault(priority, defaultMinThreshold);
        int maxThresh = maxThresholds.getOrDefault(priority, defaultMaxThreshold);
        double maxDropProb = maxDropProbabilities.getOrDefault(priority, defaultMaxDropProbability);

        if (fill < minThresh) {
            return innerQueue.enqueue(packet);
        }
        if (fill >= maxThresh) {
            return false;
        }

        double dropProbability = maxDropProb * ((double) (fill - minThresh) / (maxThresh - minThresh));
        if (Math.random() < dropProbability) {
            return false;
        }

        return innerQueue.enqueue(packet);
    }

    @Override
    public Optional<Packet> dequeue() {
        return innerQueue.dequeue();
    }

    @Override
    public int currentSize() {
        return innerQueue.currentSize();
    }
}