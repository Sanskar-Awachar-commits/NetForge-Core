import java.util.List;

public class AsciiTopologyVisualizer implements Tickable {
    private final List<NetworkNode> nodes;
    private final List<NetworkLink> links;
    private final int printIntervalTicks;

    public AsciiTopologyVisualizer(List<NetworkNode> nodes, List<NetworkLink> links, int printIntervalTicks) {
        this.nodes = nodes;
        this.links = links;
        this.printIntervalTicks = printIntervalTicks;
    }

    @Override
    public void tick(long currentTick) {
        if (currentTick % printIntervalTicks != 0) return;
        
        System.out.println("=== Simulation Tick: " + currentTick + " ===");
        System.out.println("--- Node Queues ---");
        for (var node : nodes) {
            int current = node.getQueueSize();
            int max = node.getMaxCapacity();
            String bar = generateBar(current, max);
            System.out.printf("  %s: [%s] %d/%d%n", node.getId(), bar, current, max);
        }
        
        System.out.println("--- Link Status ---");
        for (var link : links) {
            System.out.printf("  %s -> %s | In-Flight: %d | Throughput: %.1f pps%n",
                link.getSourceId(), link.getTargetId(), link.getInFlightCount(), link.getInstantaneousThroughput());
        }
        System.out.println("========================================");
    }

    private String generateBar(int current, int max) {
        int total = 10;
        int filled = max == 0 ? 0 : Math.min(total, (current * total) / max);
        return "#".repeat(filled) + ".".repeat(total - filled);
    }
}