import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public interface RoutingTable {
    String route(Packet packet);
}


public final class PrefixRoutingTable implements RoutingTable {

    private final Map<String, String> prefixRouteMap;
    private final String defaultInterfaceId;

    /**
     * Constructs a routing table with prefix rules and a default fallback interface.
     *
     * @param prefixToInterfaceId Explicit prefix-to-interface mappings
     * @param defaultInterfaceId Fallback interface if no prefix matches
     */
    public PrefixRoutingTable(Map<String, String> prefixToInterfaceId, String defaultInterfaceId) {
        Objects.requireNonNull(prefixToInterfaceId, "Prefix mappings cannot be null");
        this.defaultInterfaceId = Objects.requireNonNull(defaultInterfaceId, "Default interface cannot be null");
        
        // Sort keys by descending length to guarantee Longest Prefix Match (LPM) logic
        Map<String, String> sortedMap = new TreeMap<>((a, b) -> {
            int lenCompare = Integer.compare(b.length(), a.length());
            return lenCompare != 0 ? lenCompare : a.compareTo(b);
        });
        
        sortedMap.putAll(prefixToInterfaceId);
        this.prefixRouteMap = Collections.unmodifiableMap(sortedMap);
    }

    /**
     * Evaluates a packet's ID against the prefix table.
     * Evaluates in descending order of prefix length for deterministic correctness.
     *
     * @param packet The packet requiring structural routing
     * @return The target interface ID string
     */
    @Override
    public String route(Packet packet) {
        Objects.requireNonNull(packet, "Cannot route a null packet reference");
        String packetId = packet.id();
        
        if (packetId != null) {
            for (Map.Entry<String, String> entry : prefixRouteMap.entrySet()) {
                if (packetId.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        
        return defaultInterfaceId;
    }
}