public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing NetForge-Core Simulation...");

        SimulationEngine engine = new SimulationEngine();

        QueuePolicy queue = new BasicFIFOQueue(10);

        TrafficShaper shaper = new TrafficShaper() {
            @Override
            public boolean evaluate(Packet packet, long currentTick) {
                return true;
            }
        };

        NetworkNode node = new NetworkNode(queue, shaper);

        ConstantTrafficGenerator generator = new ConstantTrafficGenerator(node, 2);

        engine.registerNode(node);
        engine.registerGenerator(generator);

        System.out.println("Running simulation for 50 ticks...");
        engine.run(50);

        System.out.println("Simulation completed successfully.");
    }
}