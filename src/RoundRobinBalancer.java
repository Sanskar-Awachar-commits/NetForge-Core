import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NetForge-Core Round-Robin Traffic Balancer.
 * Distributes network packets evenly across parallel target interfaces.
 */
public class RoundRobinBalancer implements RoutingTable {
    private final List<String> targetInterfaceIds;
    private final AtomicInteger pointer;

    /**
     * Constructs the balancer with a immutable copy of target interfaces.
     *
     * @param targetInterfaceIds List of available outgoing interface identifiers
     */
    public RoundRobinBalancer(List<String> targetInterfaceIds) {
        Objects.requireNonNull(targetInterfaceIds, "Target interface list cannot be null");
        if (targetInterfaceIds.isEmpty()) {
            throw new IllegalArgumentException("Target interface list cannot be empty");
        }
        // Store an unmodifiable copy to ensure immutability and thread safety
        this.targetInterfaceIds = List.copyOf(targetInterfaceIds);
        this.pointer = new AtomicInteger(0);
    }

    /**
     * Determines the next interface ID using a thread-safe round-robin strategy.
     *
     * @param packet The packet requiring routing
     * @return The target interface ID string
     */
    @Override
    public String route(Packet packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        
        int currentPointer = pointer.getAndIncrement();
        
        // Bitwise AND strips the sign bit, protecting against negative values 
        // if the AtomicInteger overflows Integer.MAX_VALUE over long simulations.
        int targetIndex = (currentPointer & Integer.MAX_VALUE) % targetInterfaceIds.size();
        
        return targetInterfaceIds.get(targetIndex);
    }
}