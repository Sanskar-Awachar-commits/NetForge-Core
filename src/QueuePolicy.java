package src;
import java.util.Optional;

public interface QueuePolicy {
    boolean enqueue(Packet packet);

    Optional<Packet> dequeue();

    int currentSize();
}