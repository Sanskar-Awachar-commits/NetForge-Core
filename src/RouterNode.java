import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@FunctionalInterface
public interface RoutingTable {
    String route(Packet packet);
}

public class RouterNode implements Tickable, NetworkNode {
    private final QueuePolicy queue;
    private final TrafficShaper shaper;
    private final RoutingTable routingTable;
    private final Map<String, NetworkNode> outboundInterfaces;

    public RouterNode(QueuePolicy queue, 
                      TrafficShaper shaper, 
                      RoutingTable routingTable, 
                      Map<String, NetworkNode> outboundInterfaces) {
        this.queue = Objects.requireNonNull(queue);
        this.shaper = Objects.requireNonNull(shaper);
        this.routingTable = Objects.requireNonNull(routingTable);
        this.outboundInterfaces = Map.copyOf(outboundInterfaces);
    }

    @Override
    public void receivePacket(Packet packet) {
        queue.enqueue(packet);
    }

    @Override
    public void tick(long currentTick) {
        Optional<Packet> optimalPacket = queue.dequeue();
        if (optimalPacket.isEmpty()) {
            return;
        }

        Packet packet = optimalPacket.get();
        if (shaper.evaluate(packet, currentTick)) {
            String targetInterface = routingTable.route(packet);
            if (targetInterface != null) {
                NetworkNode nextHop = outboundInterfaces.get(targetInterface);
                if (nextHop != null) {
                    nextHop.receivePacket(packet);
                }
            }
        }
    }
}