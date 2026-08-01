import java.util.List;
import java.util.Objects;

/**
 * FirewallShaper acts as a security filter that intercepts packets before
 * passing them downstream to an underlying traffic shaping mechanism.
 */
public class FirewallShaper implements TrafficShaper {

    private final TrafficShaper innerShaper;
    private final List<String> bannedKeywords;

    /**
     * Constructs a FirewallShaper with a downstream shaper and a list of blacklisted keywords.
     *
     * @param innerShaper    The downstream TrafficShaper to delegate to if validation passes
     * @param bannedKeywords A list of substring phrases that trigger a security drop
     */
    public FirewallShaper(TrafficShaper innerShaper, List<String> bannedKeywords) {
        this.innerShaper = Objects.requireNonNull(innerShaper, "Inner shaper cannot be null");
        this.bannedKeywords = List.copyOf(bannedKeywords); // Defensive copy for immutability
    }

    /**
     * Inspects the packet ID against the banned keyword list. Drops the packet if a match 
     * is found; otherwise, delegates evaluation to the wrapped innerShaper.
     */
    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        
        String packetId = packet.id();
        if (packetId != null) {
            for (String keyword : bannedKeywords) {
                if (packetId.contains(keyword)) {
                    // Logging block action without GUI/I/O blocking via standard stream emission
                    System.out.println("Firewall BLOCKED packet [" + packetId + "]");
                    return false;
                }
            }
        }

        return innerShaper.evaluate(packet, currentTick);
    }
}