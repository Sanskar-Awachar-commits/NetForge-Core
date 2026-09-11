import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class HopLimitShaper implements TrafficShaper {
    private final int defaultTtl;
    private final Consumer<Packet> icmpResponseTarget;
    private final Map<String, Integer> hopRegistry = new ConcurrentHashMap<>();

    public HopLimitShaper(int defaultTtl, Consumer<Packet> icmpResponseTarget) {
        this.defaultTtl = defaultTtl;
        this.icmpResponseTarget = icmpResponseTarget;
    }

    public HopLimitShaper(int defaultTtl) {
        this(defaultTtl, null);
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        int currentHops = hopRegistry.compute(packet.id(), (id, hops) -> hops == null ? 1 : hops + 1);
        int remainingTtl = defaultTtl - currentHops + 1;

        if (remainingTtl <= 1) {
            hopRegistry.remove(packet.id());
            System.out.println("Packet " + packet.id() + " TTL expired! Dropping loop packet.");

            if (icmpResponseTarget != null) {
                Packet icmpPacket = new Packet(
                    "icmp-time-exceeded-" + packet.id(),
                    64,
                    currentTick,
                    8
                );
                icmpResponseTarget.accept(icmpPacket);
            }
            return false;
        }

        return true;
    }
}