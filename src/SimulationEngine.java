package src;
import java.util.ArrayList;
import java.util.List;

// Tickable.java
@FunctionalInterface
public interface Tickable {
    /**
     * Advances the component's state by one deterministic time unit.
     * @param currentTick The current simulation time tick
     */
    void tick(long currentTick);
}

// SimulationEngine.java
public class SimulationEngine {
    private final List<Tickable> components;
    private long currentTick;

    public SimulationEngine() {
        this.components = new ArrayList<>();
        this.currentTick = 0L;
    }

    public void register(Tickable component) {
        if (component != null) {
            this.components.add(component);
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