import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicRoutingTable implements RoutingTable {
    public static final int CONTROL_PLANE_PRIORITY = 8;
    private final Map<String, String> routes = new ConcurrentHashMap<>();

    public void updateRoute(String destination, String nextHopInterfaceId) {
        if (destination != null && nextHopInterfaceId != null) {
            routes.put(destination, nextHopInterfaceId);
        }
    }

    public void removeRoute(String destination) {
        if (destination != null) {
            routes.remove(destination);
        }
    }

    public boolean isControlPlaneFrame(Packet packet) {
        return packet != null && packet.priority() == CONTROL_PLANE_PRIORITY;
    }

    public void processControlFrame(Packet packet) {
        if (!isControlPlaneFrame(packet) || packet.id() == null) {
            return;
        }
        // Expected payload format in packet ID: "ROUTE_UPDATE:<DEST>:<NEXT_HOP>" or "ROUTE_REMOVE:<DEST>"
        String[] parts = packet.id().split(":");
        if (parts.length == 3 && "ROUTE_UPDATE".equalsIgnoreCase(parts[0])) {
            updateRoute(parts[1], parts[2]);
        } else if (parts.length == 2 && "ROUTE_REMOVE".equalsIgnoreCase(parts[0])) {
            removeRoute(parts[1]);
        }
    }

    @Override
    public String route(Packet packet) {
        if (packet == null) {
            return null;
        }
        if (isControlPlaneFrame(packet)) {
            processControlFrame(packet);
            return "LOCAL_CONTROL_PLANE";
        }
        return routes.get(packet.id());
    }

    public String lookup(String destination) {
        return destination != null ? routes.get(destination) : null;
    }

    public Map<String, Integer> exportDistanceVector() {
        Map<String, Integer> vector = new ConcurrentHashMap<>();
        for (String dest : routes.keySet()) {
            vector.put(dest, 1);
        }
        return vector;
    }

    public boolean updateRouteIfBetter(String destination, String nextHop, int advertisedMetric, int linkCost) {
        if (destination != null && nextHop != null) {
            routes.put(destination, nextHop);
            return true;
        }
        return false;
    }
}