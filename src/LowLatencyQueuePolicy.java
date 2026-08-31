import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

public final class LowLatencyQueuePolicy implements QueuePolicy {
    private static final int STRICT_PRIORITY_THRESHOLD = 8;
    private final int strictCapacity;
    private final Queue<Packet> strictPriorityQueue;
    private final QueuePolicy fairQueue;

    public LowLatencyQueuePolicy(int strictCapacity, QueuePolicy fairQueue) {
        if (strictCapacity <= 0) {
            throw new IllegalArgumentException("Strict queue capacity must be positive");
        }
        this.strictCapacity = strictCapacity;
        this.strictPriorityQueue = new ArrayDeque<>(strictCapacity);
        this.fairQueue = Objects.requireNonNull(fairQueue, "Fair queue policy cannot be null");
    }

    @Override
    public boolean enqueue(Packet packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        if (packet.priority() >= STRICT_PRIORITY_THRESHOLD) {
            if (strictPriorityQueue.size() >= strictCapacity) {
                return false;
            }
            return strictPriorityQueue.offer(packet);
        }
        return fairQueue.enqueue(packet);
    }

    @Override
    public Optional<Packet> dequeue() {
        if (!strictPriorityQueue.isEmpty()) {
            return Optional.ofNullable(strictPriorityQueue.poll());
        }
        return fairQueue.dequeue();
    }

    @Override
    public int currentSize() {
        return strictPriorityQueue.size() + fairQueue.currentSize();
    }
}