import java.util.Objects;

/**
 * Monitors link health and dynamic congestion to compute an updated cost weight metric.
 */
public final class LinkCostMonitor implements Tickable {

    private final NetworkLink targetLink;
    private final int baseDelayCost;
    private final int queueCostMultiplier;
    private int calculatedCost;

    public LinkCostMonitor(NetworkLink targetLink, int baseDelayCost, int queueCostMultiplier) {
        this.targetLink = Objects.requireNonNull(targetLink, "targetLink cannot be null");
        this.baseDelayCost = Math.max(1, baseDelayCost);
        this.queueCostMultiplier = Math.max(1, queueCostMultiplier);
        this.calculatedCost = this.baseDelayCost;
    }

    public LinkCostMonitor(NetworkLink targetLink) {
        this(targetLink, 10, 2);
    }

    @Override
    public void tick(long currentTick) {
        int activeQueueSize = targetLink.getQueueSize();
        int inFlightBytes = targetLink.getInFlightBytes();

        // Queue occupancy and in-flight congestion scale the base propagation cost weight
        int bufferCost = activeQueueSize * queueCostMultiplier;
        int flightCost = inFlightBytes / 1500; // Scaled per standard MTU frame

        this.calculatedCost = baseDelayCost + bufferCost + flightCost;
    }

    /**
     * @return The latest computed routing cost metric for the target link.
     */
    public int getCalculatedCost() {
        return this.calculatedCost;
    }

    public NetworkLink getTargetLink() {
        return this.targetLink;
    }
}