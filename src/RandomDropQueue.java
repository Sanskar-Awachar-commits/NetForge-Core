import java.util.Objects;
import java.util.Optional;

public final class RandomDropQueue implements QueuePolicy {

    private final QueuePolicy innerQueue;
    private final double dropProbability;

    public RandomDropQueue(QueuePolicy innerQueue, double dropProbability) {
        if (dropProbability < 0.0 || dropProbability > 1.0) {
            throw new IllegalArgumentException("Drop probability must be between 0.0 and 1.0 inclusive.");
        }
        this.innerQueue = Objects.requireNonNull(innerQueue, "Inner queue policy cannot be null.");
        this.dropProbability = dropProbability;
    }

    @Override
    public boolean enqueue(Packet packet) {
        Objects.requireNonNull(packet, "Cannot enqueue a null packet.");
        if (Math.random() < dropProbability) {
            return false; 
        }
        return innerQueue.enqueue(packet);
    }

    @Override
    public Optional<Packet> dequeue() {
        return innerQueue.dequeue();
    }

    @Override
    public int currentSize() {
        return innerQueue.currentSize();
    }
}