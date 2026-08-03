import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A network node that replicates and broadcasts enqueued packets 
 * to all downstream connections on every simulation tick.
 */
public class BroadcastNode implements Tickable, NetworkNode {
    private final QueuePolicy queuePolicy;
    private final List<NetworkNode> downstreams;

    /**
     * Constructs a BroadcastNode with a specified queue policy.
     *
     * @param queuePolicy The buffer management strategy to use
     */
    public BroadcastNode(QueuePolicy queuePolicy) {
        this.queuePolicy = Objects.requireNonNull(queuePolicy, "QueuePolicy cannot be null");
        this.downstreams = new ArrayList<>();
    }

    /**
     * Adds a downstream connection to receive broadcasted packets.
     *
     * @param node The downstream network node
     */
    public void addDownstream(NetworkNode node) {
        if (node != null && node != this) {
            downstreams.add(node);
        }
    }

    @Override
    public boolean receivePacket(Packet packet) {
        Objects.requireNonNull(packet, "Cannot receive a null packet");
        return queuePolicy.enqueue(packet);
    }

    @Override
    public void tick(long currentTick) {
        Optional<Packet> packetOpt = queuePolicy.dequeue();
        
        if (packetOpt.isPresent()) {
            Packet packetToBroadcast = packetOpt.get();
            
            for (NetworkNode downstream : downstreams) {
                // Creates an exact clone using the immutable record components
                Packet clonedPacket = new Packet(
                    packetToBroadcast.id(),
                    packetToBroadcast.sizeBytes(),
                    packetToBroadcast.creationTick(),
                    packetToBroadcast.priority()
                );
                
                downstream.receivePacket(clonedPacket);
            }
        }
    }

    /**
     * Exposes the immutable list of downstream nodes for testing or inspection.
     *
     * @return List of downstream connections
     */
    public List<NetworkNode> getDownstreams() {
        return List.copyOf(downstreams);
    }

    /**
     * @return Current occupancy of the internal queue policy
     */
    public int getQueueSize() {
        return queuePolicy.currentSize();
    }
}