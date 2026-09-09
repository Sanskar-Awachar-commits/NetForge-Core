import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public final class TapNode extends NetworkNode {
    private final NetworkNode primaryNode;
    private final NetworkNode analyticsSink;
    private final Queue<Packet> mirrorBuffer;
    private final int mirrorCapacity;

    public TapNode(NetworkNode primaryNode, NetworkNode analyticsSink, int mirrorCapacity) {
        super("TapNode");
        this.primaryNode = Objects.requireNonNull(primaryNode, "primaryNode cannot be null");
        this.analyticsSink = Objects.requireNonNull(analyticsSink, "analyticsSink cannot be null");
        this.mirrorCapacity = mirrorCapacity;
        this.mirrorBuffer = new ArrayDeque<>(Math.max(16, mirrorCapacity));
    }

    public TapNode(NetworkNode primaryNode, NetworkNode analyticsSink) {
        this(primaryNode, analyticsSink, 1024);
    }

    @Override
    public boolean receivePacket(Packet packet, long currentTick) {
        if (packet == null) {
            return false;
        }

        // TAP mirroring path: best-effort non-blocking isolation
        mirrorPacket(packet);

        // Primary path transmission guarantees zero disruption
        try {
            return primaryNode.receivePacket(packet, currentTick);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean receivePacket(Packet packet) {
        return receivePacket(packet, 0L);
    }

    @Override
    public boolean receive(Packet packet, long currentTick) {
        return receivePacket(packet, currentTick);
    }

    @Override
    public boolean receive(Packet packet) {
        return receivePacket(packet, 0L);
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

        primaryNode.tick(currentTick);
        analyticsSink.tick(currentTick);
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

    public NetworkNode getPrimaryNode() {
        return primaryNode;
    }

    public NetworkNode getAnalyticsSink() {
        return analyticsSink;
    }
}