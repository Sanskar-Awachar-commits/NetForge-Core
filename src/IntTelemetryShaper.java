/**
 * IntTelemetryShaper acts as an In-Band Network Telemetry (INT) recorder.
 * Instead of policing bandwidth, it passively augments passing packets with 
 * hop metadata (Node ID and timing) to track the packet's path and delay.
 */
public class IntTelemetryShaper implements TrafficShaper {

    private final String nodeId;
    private final int nodeProcessingDelay;
    private long totalTelemetryBytesAdded;
    private Packet lastAugmentedPacket;

    /**
     * Constructs a new INT hop recorder.
     * 
     * @param nodeId Unique identifier for this network node
     * @param nodeProcessingDelay The simulated delay added by this node's processing
     */
    public IntTelemetryShaper(String nodeId, int nodeProcessingDelay) {
        this.nodeId = nodeId;
        this.nodeProcessingDelay = nodeProcessingDelay;
        this.totalTelemetryBytesAdded = 0L;
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        // Construct the telemetry trailer
        String telemetryTrailer = "|HOP=" + nodeId + "@" + (currentTick + nodeProcessingDelay);
        int telemetryOverheadBytes = telemetryTrailer.length();

        // Construct the augmented packet record with updated ID and size
        this.lastAugmentedPacket = new Packet(
            packet.id() + telemetryTrailer,
            packet.sizeBytes() + telemetryOverheadBytes,
            packet.creationTick(),
            packet.priority()
        );

        // Track the cumulative telemetry overhead injected by this node
        this.totalTelemetryBytesAdded += telemetryOverheadBytes;

        // Always return true to allow the packet to continue through the pipeline
        return true;
    }

    /**
     * @return The total number of telemetry bytes injected by this node.
     */
    public long getTotalTelemetryBytesAdded() {
        return totalTelemetryBytesAdded;
    }

    /**
     * Exposes the most recently augmented packet so the pipeline/buffer
     * can retrieve the newly constructed record since pass-by-value prevents 
     * in-place mutation of the original Packet record.
     * 
     * @return The augmented INT packet
     */
    public Packet getLastAugmentedPacket() {
        return lastAugmentedPacket;
    }
}