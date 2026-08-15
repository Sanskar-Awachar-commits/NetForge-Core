import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class SinkNode extends NetworkNode {
    private long totalPacketsReceived = 0;
    private long totalLatencyTicks = 0;
    private long duplicateCount = 0;
    private long droppedCount = 0;
    private final Set<String> seenPacketIds = new HashSet<>();

    public SinkNode() {
        super("SinkNode");
    }

    public SinkNode(String name) {
        super(name);
    }

    @Override
    public boolean receivePacket(Packet packet, long currentTick) {
        if (packet == null) {
            droppedCount++;
            return false;
        }

        long latency = currentTick - packet.creationTick();
        if (latency < 0) {
            latency = 0;
        }

        totalPacketsReceived++;
        totalLatencyTicks += latency;

        if (packet.id().endsWith("_DUP") || seenPacketIds.contains(packet.id())) {
            duplicateCount++;
        } else {
            seenPacketIds.add(packet.id());
        }

        return true;
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
        // Sink nodes terminate packets and do not forward downstream
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

    public int getReceivedCount() {
        return (int) totalPacketsReceived;
    }

    public long getTotalLatencyTicks() {
        return totalLatencyTicks;
    }

    public Telemetry getTelemetry() {
        return new Telemetry(totalPacketsReceived, duplicateCount, droppedCount);
    }
}