import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

public interface Tickable {
    void tick(long currentTick);
}

public class DuplicatingNetworkLink implements Tickable {
    private final Consumer<Packet> destination;
    private final double duplicationProbability;
    private final long propagationDelayTicks;
    private final Queue<InFlightPacket> propagationQueue = new ArrayDeque<>();

    private record InFlightPacket(Packet packet, long arrivalTick) {}

    public DuplicatingNetworkLink(Consumer<Packet> destination, double duplicationProbability, long propagationDelayTicks) {
        this.destination = Objects.requireNonNull(destination, "Destination cannot be null");
        if (duplicationProbability < 0.0 || duplicationProbability > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }
        this.duplicationProbability = duplicationProbability;
        this.propagationDelayTicks = propagationDelayTicks;
    }

    public void send(Packet p, long currentTick) {
        long arrivalTick = currentTick + propagationDelayTicks;
        propagationQueue.add(new InFlightPacket(p, arrivalTick));

        if (Math.random() < duplicationProbability) {
            Packet duplicate = new Packet(
                p.id() + "_DUP",
                p.sizeBytes(),
                p.creationTick(),
                p.priority()
            );
            propagationQueue.add(new InFlightPacket(duplicate, arrivalTick));
        }
    }

    @Override
    public void tick(long currentTick) {
        while (!propagationQueue.isEmpty() && propagationQueue.peek().arrivalTick() <= currentTick) {
            InFlightPacket inFlight = propagationQueue.poll();
            destination.accept(inFlight.packet());
        }
    }

    public int getInFlightCount() {
        return propagationQueue.size();
    }
}