import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoutingUpdateProcessor implements TrafficShaper {
    public static final int CONTROL_PLANE_PRIORITY = 8;
    private final DynamicRoutingTable routingTable;
    private final int localLinkCost;

    public record RouteEntry(String nextHopInterfaceId, int metric) {}

    public static class DynamicRoutingTable {
        private final Map<String, RouteEntry> routes = new ConcurrentHashMap<>();

        public synchronized boolean updateRouteIfBetter(String destination, String nextHop, int advertisedMetric, int linkCost) {
            int totalCost = advertisedMetric + linkCost;
            RouteEntry current = routes.get(destination);
            if (current == null || totalCost < current.metric()) {
                routes.put(destination, new RouteEntry(nextHop, totalCost));
                return true;
            }
            return false;
        }

        public RouteEntry getRoute(String destination) {
            return routes.get(destination);
        }
    }

    public RoutingUpdateProcessor(DynamicRoutingTable routingTable, int localLinkCost) {
        this.routingTable = routingTable;
        this.localLinkCost = localLinkCost;
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        if (packet.priority() != CONTROL_PLANE_PRIORITY) {
            return true;
        }

        // Expected control ID payload format: "CTRL:<destination>:<neighborInterface>:<advertisedMetric>"
        String[] tokens = packet.id().split(":");
        if (tokens.length == 4 && "CTRL".equals(tokens[0])) {
            String destination = tokens[1];
            String neighborInterface = tokens[2];
            try {
                int advertisedMetric = Integer.parseInt(tokens[3]);
                routingTable.updateRouteIfBetter(destination, neighborInterface, advertisedMetric, localLinkCost);
            } catch (NumberFormatException ignored) {
                // Drop malformed control packet silently
            }
        }

        // Intercept and swallow control-plane frames to prevent data-plane leakage
        return false;
    }
}