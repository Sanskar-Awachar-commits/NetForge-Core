import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public final class TapNode implements NetworkNode, Tickable {
    private final NetworkNode primaryNode;
    private final NetworkNode analyticsSink;
    private final Queue<Packet> mirrorBuffer;
    private final int mirrorCapacity;

    public TapNode(NetworkNode primaryNode, NetworkNode analyticsSink, int mirrorCapacity) {
        this.primaryNode = Objects.requireNonNull(primaryNode, "primaryNode cannot be null");
        this.analyticsSink = Objects.requireNonNull(analyticsSink, "analyticsSink cannot be null");
        this.mirrorCapacity = mirrorCapacity;
        this.mirrorBuffer = new ArrayDeque<>(Math.max(16, mirrorCapacity));
    }

    public TapNode(NetworkNode primaryNode, NetworkNode analyticsSink) {
        this(primaryNode, analyticsSink, 1024);
    }

    @Override
    public void receivePacket(Packet packet, long currentTick) {
        Objects.requireNonNull(packet, "packet cannot be null");

        // Primary path transmission guarantees zero disruption
        try {
            primaryNode.receivePacket(packet, currentTick);
        } finally {
            // TAP mirroring path: best-effort non-blocking isolation
            mirrorPacket(packet);
        }
    }

    @Override
    public void tick(long currentTick) {
        // Drain pending mirrored packets to the analytics sink
        while (!mirrorBuffer.isEmpty()) {
            Packet mirrored = mirrorBuffer.poll();
            try {
                analyticsSink.receivePacket(mirrored, currentTick);
            } catch (Exception ignored) {
                // Drop mirrored telemetry if sink encounters failures
            }
        }

        if (primaryNode instanceof Tickable tickablePrimary) {
            tickablePrimary.tick(currentTick);
        }
        if (analyticsSink instanceof Tickable tickableSink) {
            tickableSink.tick(currentTick);
        }
    }

    private void mirrorPacket(Packet original) {
        try {
            if (mirrorBuffer.size() < mirrorCapacity) {
                Packet cloned = new Packet(
                    original.id() + "[TAP_MIRROR]",
                    original.sizeBytes(),
                    original.creationTick(),
                    original.priority()
                );
                mirrorBuffer.offer(cloned);
            }
        } catch (Exception ignored) {
            // Guard primary path against internal tap allocation or queue failures
        }
    }
}