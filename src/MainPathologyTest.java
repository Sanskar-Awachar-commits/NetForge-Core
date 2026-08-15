public class MainPathologyTest {

    public static void main(String[] args) {
        System.out.println("--- Starting NetForge-Core Pathology Stress Test ---");

        // 1. Initialize unified termination point
        SinkNode sink = new SinkNode("Unified-Sink");

        // 2. Construct Path A (Jittery Link)
        JitteryNetworkLink pathA = new JitteryNetworkLink("Path-A-Jitter", sink);

        // 3. Construct Path B (Out of Order -> Duplicating chain)
        DuplicatingNetworkLink dupLink = new DuplicatingNetworkLink("Path-B-Dup", sink);
        OutOfOrderNetworkLink pathB = new OutOfOrderNetworkLink("Path-B-OoO", dupLink);

        // 4. Initialize Router and bind paths
        RouterNode router = new RouterNode("Core-Router");
        router.addPath(pathA);
        router.addPath(pathB);

        // 5. Initialize Traffic Generator feeding the router
        BurstTrafficGenerator generator = new BurstTrafficGenerator("Burst-Gen", router);

        System.out.println("Topology wired. Executing discrete-event simulation for 200 ticks...");

        // 6. Execute deterministic clock ticks
        for (long currentTick = 1; currentTick <= 200; currentTick++) {
            generator.tick(currentTick);
            router.tick(currentTick);
            pathA.tick(currentTick);
            pathB.tick(currentTick);
            dupLink.tick(currentTick);
            sink.tick(currentTick);
        }

        // 7. Extract and verify telemetry
        verifyTelemetry(sink);
    }

    private static void verifyTelemetry(SinkNode sink) {
        var telemetry = sink.getTelemetry();
        
        System.out.println("\n=== Node Telemetry Report ===");
        System.out.printf("Total Packets Processed : %d%n", telemetry.getProcessedCount());
        System.out.printf("Identified Duplicates   : %d%n", telemetry.getDuplicateCount());
        System.out.printf("Dropped Payloads        : %d%n", telemetry.getDroppedCount());
        
        // Validate engine calculus logic
        if (telemetry.getProcessedCount() == 0) {
            System.err.println("FATAL: Simulation failed. No packets arrived at SinkNode.");
        } else {
            System.out.println("SUCCESS: Application engine telemetry extraction verified.");
        }
    }
}