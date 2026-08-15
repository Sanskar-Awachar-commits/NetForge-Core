import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NetForge-Core Round-Robin Traffic Balancer.
 * Distributes network packets evenly across parallel target interfaces or nodes.
 */
public class RoundRobinBalancer implements RoutingTable {
    private final List<String> targetInterfaceIds;
    private final List<NetworkNode> targetNodes;
    private final AtomicInteger pointer;

    /**
     * Constructs the balancer with a list of target interfaces or nodes.
     *
     * @param items List of available outgoing interface identifiers or NetworkNode objects
     */
    public RoundRobinBalancer(List<?> items) {
        Objects.requireNonNull(items, "Target items list cannot be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Target list cannot be empty");
        }
        
        List<String> ids = new ArrayList<>();
        List<NetworkNode> nodes = new ArrayList<>();

        for (Object item : items) {
            if (item instanceof NetworkNode node) {
                nodes.add(node);
                ids.add(node.getName());
            } else if (item != null) {
                ids.add(item.toString());
            }
        }

        this.targetInterfaceIds = List.copyOf(ids);
        this.targetNodes = List.copyOf(nodes);
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
        if (targetInterfaceIds.isEmpty()) {
            return null;
        }
        
        int currentPointer = pointer.getAndIncrement();
        int targetIndex = (currentPointer & Integer.MAX_VALUE) % targetInterfaceIds.size();
        
        return targetInterfaceIds.get(targetIndex);
    }

    /**
     * Retrieves the next target node using round-robin distribution.
     *
     * @return The next NetworkNode or null if nodes list is empty
     */
    public NetworkNode nextNode() {
        if (targetNodes.isEmpty()) {
            return null;
        }
        int currentPointer = pointer.getAndIncrement();
        int targetIndex = (currentPointer & Integer.MAX_VALUE) % targetNodes.size();
        return targetNodes.get(targetIndex);
    }

    public List<NetworkNode> getTargetNodes() {
        return targetNodes;
    }

    public List<String> getTargetInterfaceIds() {
        return targetInterfaceIds;
    }
}