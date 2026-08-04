import java.util.List;

public class MainRoutingTest {
    public static void main(String[] args) {
        SimulationEngine engine = new SimulationEngine();

        SinkNode sinkA = new SinkNode("Sink-A");
        SinkNode sinkB = new SinkNode("Sink-B");

        NetworkNode targetA = new NetworkNode("Node-A", sinkA);
        NetworkNode targetB = new NetworkNode("Node-B", sinkB);

        RoundRobinBalancer balancer = new RoundRobinBalancer(List.of(targetA, targetB));
        RouterNode router = new RouterNode("Router-Core", balancer);

        BurstTrafficGenerator generator = new BurstTrafficGenerator(router, 10, 0);
        
        engine.registerGenerator(generator);
        engine.registerNode(router);
        engine.registerNode(targetA);
        engine.registerNode(targetB);
        engine.registerNode(sinkA);
        engine.registerNode(sinkB);

        engine.run(20);

        int countA = sinkA.getReceivedCount();
        int countB = sinkB.getReceivedCount();

        System.out.printf("Sink A Received: %d packets%n", countA);
        System.out.printf("Sink B Received: %d packets%n", countB);

        if (countA == 5 && countB == 5) {
            System.out.println("VERIFICATION SUCCESS: Traffic perfectly interleaved and distributed 50/50.");
        } else {
            throw new AssertionError(
                String.format("VERIFICATION FAILED: Expected 5/5 split, got %d/%d", countA, countB)
            );
        }
    }
}