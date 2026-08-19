package netforge.core.routing;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Periodically broadcasts local topological distance vectors as control-plane packets.
 */
public final class DistanceVectorAgent implements Tickable {
    private final String nodeId;
    private final DynamicRoutingTable routingTable;
    private final List<Consumer<Packet>> neighborLinks;
    private final long broadcastIntervalTicks;
    private long lastBroadcastTick;

    public DistanceVectorAgent(
            String nodeId,
            DynamicRoutingTable routingTable,
            List<Consumer<Packet>> neighborLinks,
            long broadcastIntervalTicks) {
        this.nodeId = Objects.requireNonNull(nodeId, "nodeId cannot be null");
        this.routingTable = Objects.requireNonNull(routingTable, "routingTable cannot be null");
        this.neighborLinks = List.copyOf(neighborLinks);
        this.broadcastIntervalTicks = broadcastIntervalTicks;
        this.lastBroadcastTick = -broadcastIntervalTicks;
    }

    @Override
    public void tick(long currentTick) {
        if (currentTick - lastBroadcastTick >= broadcastIntervalTicks) {
            lastBroadcastTick = currentTick;
            broadcastRoutingHorizon(currentTick);
        }
    }

    private void broadcastRoutingHorizon(long currentTick) {
        String serializedVector = serializeTable();
        int payloadSize = serializedVector.getBytes(StandardCharsets.UTF_8).length;
        
        Packet controlFrame = new Packet(
                "CTRL-DV-" + nodeId + "-" + currentTick + "-" + UUID.randomUUID().toString().substring(0, 8),
                payloadSize,
                currentTick,
                8
        );

        for (Consumer<Packet> link : neighborLinks) {
            link.accept(controlFrame);
        }
    }

    private String serializeTable() {
        StringBuilder builder = new StringBuilder("DV|SRC=").append(nodeId).append("|ENTRIES=");
        Map<String, Integer> distances = routingTable.exportDistanceVector();
        distances.forEach((dest, metric) -> builder.append(dest).append(":").append(metric).append(";"));
        return builder.toString();
    }
}