import java.util.Optional;

/**
 * ChaosNode - Simulates a network node that periodically goes offline (flaps).
 * When online, it dequeues and processes packets from its internal queue.
 * When offline, processing halts, causing packet backlogs and tail-drops.
 */
public class ChaosNode extends NetworkNode {
    private final QueuePolicy queue;
    private final int flapIntervalTicks;
    private Packet currentProcessedPacket;

    public ChaosNode(QueuePolicy queue, int flapIntervalTicks) {
        super("ChaosNode", queue, (packet, tick) -> true);
        if (flapIntervalTicks <= 0) {
            throw new IllegalArgumentException("Flap interval must be greater than 0 ticks.");
        }
        this.queue = queue;
        this.flapIntervalTicks = flapIntervalTicks;
    }

    @Override
    public void tick(long currentTick) {
        // Calculate node availability based on current tick window
        boolean isOnline = (currentTick / flapIntervalTicks) % 2 == 0;

        if (!isOnline) {
            // Node is offline/flapping: Skip processing completely to induce backlogs
            return;
        }

        // Node is online: Dequeue and process the next packet
        Optional<Packet> nextPacket = queue.dequeue();
        if (nextPacket.isPresent()) {
            this.currentProcessedPacket = nextPacket.get();
            processPacket(this.currentProcessedPacket, currentTick);
        }
    }

    private void processPacket(Packet packet, long currentTick) {
        // Deterministic processing logic for simulated packet transmission
        if (getConnectedNode() != null) {
            getConnectedNode().receivePacket(packet, currentTick);
        }
    }

    public boolean enqueue(Packet packet) {
        return queue.enqueue(packet);
    }

    public boolean isOnline(long currentTick) {
        return (currentTick / flapIntervalTicks) % 2 == 0;
    }

    public QueuePolicy getQueue() {
        return queue;
    }
}