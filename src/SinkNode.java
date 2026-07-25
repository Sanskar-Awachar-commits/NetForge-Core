import java.util.Objects;

public final class SinkNode implements Tickable {
    private long totalPacketsReceived = 0;
    private long totalLatencyTicks = 0;
    public void receivePacket(Packet packet, long currentTick) {
        Objects.requireNonNull(packet, "Cannot process a null packet.");
        
        long latency = currentTick - packet.creationTick();
        if (latency < 0) {
            latency = 0;
        }

        totalPacketsReceived++;
        totalLatencyTicks += latency;
    }

    @Override
    public void tick(long currentTick) {
    }
    public void printStats() {
        double averageLatency = totalPacketsReceived == 0 
            ? 0.0 
            : (double) totalLatencyTicks / totalPacketsReceived;

        System.out.println("=== NetForge-Core Sink Node Statistics ===");
        System.out.printf("Total Packets Received : %d%n", totalPacketsReceived);
        System.out.printf("Total Latency (Ticks)  : %d%n", totalLatencyTicks);
        System.out.printf("Average Latency/Packet : %.2f ticks%n", averageLatency);
        System.out.println("==========================================");
    }

    public long getTotalPacketsReceived() {
        return totalPacketsReceived;
    }

    public long getTotalLatencyTicks() {
        return totalLatencyTicks;
    }
}