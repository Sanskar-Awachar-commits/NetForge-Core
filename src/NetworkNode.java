import java.util.Objects;
import java.util.Optional;

interface Tickable {
    void tick(long currentTick);
}

public class NetworkNode implements Tickable {
    private final QueuePolicy queuePolicy;
    private final TrafficShaper trafficShaper;

    public NetworkNode(QueuePolicy queuePolicy, TrafficShaper trafficShaper) {
        this.queuePolicy = Objects.requireNonNull(queuePolicy, "QueuePolicy cannot be null");
        this.trafficShaper = Objects.requireNonNull(trafficShaper, "TrafficShaper cannot be null");
    }

    public boolean receive(Packet packet) {
        return queuePolicy.enqueue(packet);
    }

    @Override
    public void tick(long currentTick) {
        queuePolicy.dequeue().ifPresent(packet -> {
            if (trafficShaper.evaluate(packet, currentTick)) {
                System.out.println("Packet " + packet.id() + " processed");
            } else {
                System.out.println("Packet " + packet.id() + " dropped by shaper");
            }
        });
    }
}