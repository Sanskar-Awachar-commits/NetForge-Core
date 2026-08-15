import java.util.Objects;
import java.util.Optional;

public class NetworkNode implements Tickable {
    private final String name;
    private final QueuePolicy queuePolicy;
    private final TrafficShaper trafficShaper;
    private NetworkNode connectedNode;

    public NetworkNode() {
        this("NetworkNode", new BasicFIFOQueue(1000), (packet, tick) -> true);
    }

    public NetworkNode(String name) {
        this(name, new BasicFIFOQueue(1000), (packet, tick) -> true);
    }

    public NetworkNode(QueuePolicy queuePolicy, TrafficShaper trafficShaper) {
        this("NetworkNode", queuePolicy, trafficShaper);
    }

    public NetworkNode(String name, QueuePolicy queuePolicy, TrafficShaper trafficShaper) {
        this.name = Objects.requireNonNull(name, "Node name cannot be null");
        this.queuePolicy = Objects.requireNonNull(queuePolicy, "QueuePolicy cannot be null");
        this.trafficShaper = Objects.requireNonNull(trafficShaper, "TrafficShaper cannot be null");
    }

    public NetworkNode(String name, NetworkNode downstream) {
        this(name, new BasicFIFOQueue(1000), (packet, tick) -> true);
        this.connectedNode = downstream;
    }

    public String getName() {
        return name;
    }

    public void connect(NetworkNode target) {
        this.connectedNode = target;
    }

    public NetworkNode getConnectedNode() {
        return connectedNode;
    }

    public QueuePolicy getQueuePolicy() {
        return queuePolicy;
    }

    public TrafficShaper getTrafficShaper() {
        return trafficShaper;
    }

    public boolean receive(Packet packet) {
        return receivePacket(packet);
    }

    public boolean receive(Packet packet, long currentTick) {
        return receivePacket(packet, currentTick);
    }

    public boolean receivePacket(Packet packet) {
        return receivePacket(packet, 0L);
    }

    public boolean receivePacket(Packet packet, long currentTick) {
        if (packet == null) {
            return false;
        }
        if (queuePolicy != null) {
            return queuePolicy.enqueue(packet);
        }
        return true;
    }

    @Override
    public void tick(long currentTick) {
        if (queuePolicy == null) {
            return;
        }
        Optional<Packet> packetOpt = queuePolicy.dequeue();
        if (packetOpt.isPresent()) {
            Packet packet = packetOpt.get();
            if (trafficShaper != null && trafficShaper.evaluate(packet, currentTick)) {
                System.out.println("Packet " + packet.id() + " processed");
                if (connectedNode != null) {
                    connectedNode.receivePacket(packet, currentTick);
                }
            } else if (trafficShaper != null) {
                System.out.println("Packet " + packet.id() + " dropped by shaper");
            }
        }
    }
}