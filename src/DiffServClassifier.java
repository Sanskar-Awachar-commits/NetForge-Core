import java.util.Objects;

/**
 * A Differentiated Services (DiffServ) classifier that marks packets based on size
 * before passing them to an underlying traffic shaping mechanism.
 */
public final class DiffServClassifier implements TrafficShaper {

    private final TrafficShaper innerShaper;
    private final int sizeThresholdBytes;
    private final int highPriorityValue;

    /**
     * Constructs a new DiffServClassifier.
     *
     * @param innerShaper        The downstream TrafficShaper to evaluate the packet
     * @param sizeThresholdBytes Packets strictly smaller than this value will be prioritized
     * @param highPriorityValue  The priority value assigned to qualifying expedited packets
     */
    public DiffServClassifier(TrafficShaper innerShaper, int sizeThresholdBytes, int highPriorityValue) {
        this.innerShaper = Objects.requireNonNull(innerShaper, "Inner shaper cannot be null");
        this.sizeThresholdBytes = sizeThresholdBytes;
        this.highPriorityValue = highPriorityValue;
    }

    /**
     * Re-prioritizes small packets to grant them expedited forwarding, 
     * then delegates the evaluation to the nested shaper.
     */
    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        
        Packet evaluatedPacket = packet;

        // Mutate by instantiation if the packet qualifies for expedited forwarding
        if (packet.sizeBytes() < sizeThresholdBytes) {
            evaluatedPacket = new Packet(
                packet.id(),
                packet.sizeBytes(),
                packet.creationTick(),
                highPriorityValue
            );
        }

        return innerShaper.evaluate(evaluatedPacket, currentTick);
    }
}