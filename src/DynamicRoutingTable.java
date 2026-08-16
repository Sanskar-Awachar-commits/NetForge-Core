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
    public Optional<String> route(Packet packet) {
        if (packet == null) {
            return Optional.empty();
        }
        if (isControlPlaneFrame(packet)) {
            processControlFrame(packet);
            return Optional.of("LOCAL_CONTROL_PLANE");
        }
        return Optional.ofNullable(routes.get(packet.id()));
    }

    public Optional<String> lookup(String destination) {
        return Optional.ofNullable(routes.get(destination));
    }
}

interface RoutingTable {
    Optional<String> route(Packet packet);
}