import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class BasicFIFOQueue implements QueuePolicy {

    private final Queue<Packet> queue;
    private final int maxCapacity;

    public BasicFIFOQueue(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("Queue capacity cannot be negative.");
        }
        this.queue = new LinkedList<>();
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean enqueue(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Cannot enqueue a null packet.");
        }
        
        if (queue.size() >= maxCapacity) {
            return false;
        }
        
        return queue.offer(packet);
    }

    @Override
    public Optional<Packet> dequeue() {
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public int currentSize() {
        return queue.size();
    }
}