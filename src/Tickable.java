@FunctionalInterface
public interface Tickable {
    /**
     * Advances the component's state by one deterministic time unit.
     * 
     * @param currentTick The current simulation time tick
     */
    void tick(long currentTick);
}
