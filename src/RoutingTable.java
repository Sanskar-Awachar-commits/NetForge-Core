@FunctionalInterface
public interface RoutingTable {
    /**
     * Determines the target outgoing interface or node identifier for a packet.
     * 
     * @param packet The packet requiring routing
     * @return The target identifier string, or null if unroutable
     */
    String route(Packet packet);
}
