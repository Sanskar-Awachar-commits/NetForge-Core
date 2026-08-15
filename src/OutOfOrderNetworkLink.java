import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Simulates a network link capable of reordering packets due to multi-path routing.
 */
public class OutOfOrderNetworkLink extends NetworkLink {
    private final NetworkNode targetNode;
    private final List<Packet> inFlightBuffer;

    public OutOfOrderNetworkLink(NetworkNode targetNode) {
        this("OutOfOrderNetworkLink", targetNode);
    }

    public OutOfOrderNetworkLink(String name, NetworkNode targetNode) {
        super(name, 1, targetNode);
        this.targetNode = Objects.requireNonNull(targetNode, "Target node cannot be null");
        this.inFlightBuffer = new ArrayList<>();
    }

    /**
     * Accepts a packet into the link's in-flight transmission buffer.
     * 
     * @param packet      The packet to send
     * @param currentTick The simulation tick at which the packet entered the link
     */
    @Override
    public void send(Packet packet, long currentTick) {
        if (packet != null) {
            inFlightBuffer.add(packet);
        }
    }

    @Override
    public void tick(long currentTick) {
        if (inFlightBuffer.isEmpty()) {
            return;
        }

        // Perturb buffer order on even ticks to introduce non-sequential arrival
        if ((currentTick & 1) == 0 && inFlightBuffer.size() > 1) {
            Collections.swap(inFlightBuffer, 0, 1);
        }

        Packet toDeliver = inFlightBuffer.remove(0);
        targetNode.receivePacket(toDeliver, currentTick);
    }

    public int getInFlightCount() {
        return inFlightBuffer.size();
    }
}