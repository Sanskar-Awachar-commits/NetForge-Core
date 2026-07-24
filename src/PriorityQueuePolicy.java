import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * PriorityQueuePolicy implements a strict priority-based queueing mechanism.
 * It utilizes a java.util.PriorityQueue to ensure packets with the highest 
 * priority integer are dequeued first. It enforces a maximum capacity, 
 * tail-dropping packets if the queue is full upon arrival.
 */
public class PriorityQueuePolicy implements QueuePolicy {
    private final int maxCapacity;
    private final PriorityQueue<Packet> buffer;

    public PriorityQueuePolicy(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.maxCapacity = maxCapacity;
        
        // Higher priority integer means higher urgency (dequeued first).
        // If priorities match, we preserve natural order or they are treated equally.
        this.buffer = new PriorityQueue<>(
            Comparator.comparingInt(Packet::priority).reversed()
        );
    }

    @Override
    public boolean enqueue(Packet packet) {
        if (packet == null) {
            return false;
        }
        
        // Tail-drop: If the buffer is at or beyond capacity, reject the packet
        if (buffer.size() >= maxCapacity) {
            return false;
        }
        
        return buffer.offer(packet);
    }

    @Override
    public Optional<Packet> dequeue() {
        return Optional.ofNullable(buffer.poll());
    }

    @Override
    public int currentSize() {
        return buffer.size();
    }
}