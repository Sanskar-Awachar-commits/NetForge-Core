import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {
    private final List<Tickable> components;
    private long currentTick;

    public SimulationEngine() {
        this.components = new ArrayList<>();
        this.currentTick = 0L;
    }

    public void register(Tickable component) {
        if (component != null && !components.contains(component)) {
            this.components.add(component);
        }
    }

    public void registerNode(NetworkNode node) {
        register(node);
    }

    public void registerGenerator(Tickable generator) {
        register(generator);
    }

    public void tick(long tick) {
        this.currentTick = tick;
        for (Tickable component : this.components) {
            component.tick(this.currentTick);
        }
    }

    public void run(int totalTicks) {
        for (int i = 0; i < totalTicks; i++) {
            this.currentTick++;
            for (Tickable component : this.components) {
                component.tick(this.currentTick);
            }
        }
    }

    public long getCurrentTick() {
        return this.currentTick;
    }
}