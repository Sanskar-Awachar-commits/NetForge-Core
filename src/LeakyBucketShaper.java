public class LeakyBucketShaper implements TrafficShaper {
    
    private final long bucketCapacity;
    private final double leakRatePerTick;
    
    private double currentWaterLevel;
    private long lastUpdateTick;

    /**
     * @param bucketCapacity  Maximum bytes the bucket can hold before overflowing.
     * @param leakRatePerTick Bytes leaked per tick.
     */
    public LeakyBucketShaper(long bucketCapacity, double leakRatePerTick) {
        if (bucketCapacity <= 0 || leakRatePerTick <= 0) {
            throw new IllegalArgumentException("Capacity and leak rate must be strictly positive.");
        }
        this.bucketCapacity = bucketCapacity;
        this.leakRatePerTick = leakRatePerTick;
        this.currentWaterLevel = 0.0;
        this.lastUpdateTick = -1; // Signifies uninitialized time
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        // Initialize the clock on the first packet arrival
        if (lastUpdateTick == -1) {
            lastUpdateTick = currentTick;
        }

        long ticksPassed = currentTick - lastUpdateTick;
        if (ticksPassed < 0) {
            throw new IllegalArgumentException("Simulation time cannot flow backwards.");
        }

        // 1. "Leak" the water based on elapsed time
        double leakedAmount = ticksPassed * leakRatePerTick;
        currentWaterLevel = Math.max(0.0, currentWaterLevel - leakedAmount);
        lastUpdateTick = currentTick;

        // 2. Evaluate if the new packet fits into the remaining capacity
        if (currentWaterLevel + packet.sizeBytes() <= bucketCapacity) {
            currentWaterLevel += packet.sizeBytes();
            return true;
        }

        // Packet overflows the bucket and is dropped
        return false;
    }
}