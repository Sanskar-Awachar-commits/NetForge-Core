
public class MainBurstTest {
    public static void main(String[] args) {
        SimulationEngine engine = new SimulationEngine();

        // 1. Shaper setup: 1000 bytes/tick sustained, burst capacity 5000 bytes
        TrafficShaper shaper = new ShaperLogger(new TokenBucketShaper(1000, 5000));

        // 2. Node & Queue setup
        QueuePolicy queue = new BasicFIFOQueue(100);
        NetworkNode node = new NetworkNode("IngressRouter", queue, shaper);

        // 3. Traffic Generation: 50 packets of 256 bytes every 20 ticks
        BurstTrafficGenerator gen = new BurstTrafficGenerator("Gen1", 50, 256, 20, node);

        // 4. Link & Sink Setup
        SinkNode sink = new SinkNode("EgressSink");
        NetworkLink link = new NetworkLink(5, sink); // 5 ticks propagation delay
        node.connect(link);

        // 5. Registration
        engine.register(gen);
        engine.register(node);
        engine.register(link);

        // 6. Execution loop
        for (long tick = 0; tick < 100; tick++) {
            engine.tick(tick);
        }

        sink.printStats();
    }
}