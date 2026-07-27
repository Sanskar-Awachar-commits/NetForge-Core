import java.util.Objects;

public class ShaperLogger implements TrafficShaper {
    private final TrafficShaper innerShaper;
    private final String name;

    public ShaperLogger(TrafficShaper innerShaper, String name) {
        this.innerShaper = Objects.requireNonNull(innerShaper, "Inner shaper cannot be null");
        this.name = Objects.requireNonNull(name, "Shaper name cannot be null");
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        
        boolean allowed = innerShaper.evaluate(packet, currentTick);
        
        if (!allowed) {
            System.out.printf("[%s] dropped packet %s at tick %d%n", 
                name, 
                packet.id(), 
                currentTick
            );
        }
        
        return allowed;
    }
}