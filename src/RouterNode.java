import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RouterNode extends NetworkNode {
    private final QueuePolicy queue;
    private final TrafficShaper shaper;
    private final RoutingTable routingTable;
    private final Map<String, NetworkNode> outboundInterfaces;
    private final List<NetworkNode> dynamicPaths;
    private int roundRobinPathIndex = 0;

    public RouterNode(String name) {
        super(name, new BasicFIFOQueue(1000), (packet, tick) -> true);
        this.queue = getQueuePolicy();
        this.shaper = getTrafficShaper();
        this.routingTable = null;
        this.outboundInterfaces = new HashMap<>();
        this.dynamicPaths = new ArrayList<>();
    }

    public RouterNode(String name, RoutingTable routingTable) {
        super(name, new BasicFIFOQueue(1000), (packet, tick) -> true);
        this.queue = getQueuePolicy();
        this.shaper = getTrafficShaper();
        this.routingTable = routingTable;
        this.outboundInterfaces = new HashMap<>();
        this.dynamicPaths = new ArrayList<>();
    }

    public RouterNode(QueuePolicy queue, 
                      TrafficShaper shaper, 
                      RoutingTable routingTable, 
                      Map<String, NetworkNode> outboundInterfaces) {
        super("RouterNode", Objects.requireNonNull(queue), Objects.requireNonNull(shaper));
        this.queue = queue;
        this.shaper = shaper;
        this.routingTable = Objects.requireNonNull(routingTable);
        this.outboundInterfaces = new HashMap<>(outboundInterfaces);
        this.dynamicPaths = new ArrayList<>(outboundInterfaces.values());
    }

    public void addPath(NetworkNode path) {
        if (path != null) {
            dynamicPaths.add(path);
            outboundInterfaces.put(path.getName(), path);
        }
    }

    public void addPath(String interfaceId, NetworkNode path) {
        if (path != null) {
            dynamicPaths.add(path);
            outboundInterfaces.put(interfaceId, path);
        }
    }

    @Override
    public boolean receivePacket(Packet packet) {
        return queue.enqueue(packet);
    }

    @Override
    public boolean receivePacket(Packet packet, long currentTick) {
        return queue.enqueue(packet);
    }

    @Override
    public void tick(long currentTick) {
        Optional<Packet> optimalPacket = queue.dequeue();
        if (optimalPacket.isEmpty()) {
            return;
        }

        Packet packet = optimalPacket.get();
        if (shaper.evaluate(packet, currentTick)) {
            if (routingTable instanceof RoundRobinBalancer balancer && !balancer.getTargetNodes().isEmpty()) {
                NetworkNode nextHop = balancer.nextNode();
                if (nextHop != null) {
                    nextHop.receivePacket(packet, currentTick);
                }
            } else if (routingTable != null) {
                String targetInterface = routingTable.route(packet);
                if (targetInterface != null) {
                    NetworkNode nextHop = outboundInterfaces.get(targetInterface);
                    if (nextHop != null) {
                        nextHop.receivePacket(packet, currentTick);
                    }
                }
            } else if (!dynamicPaths.isEmpty()) {
                NetworkNode nextHop = dynamicPaths.get(roundRobinPathIndex % dynamicPaths.size());
                roundRobinPathIndex++;
                if (nextHop != null) {
                    nextHop.receivePacket(packet, currentTick);
                }
            }
        }
    }
}