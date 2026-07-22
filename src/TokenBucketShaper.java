public class TokenBucketShaper implements TrafficShaper {
    private final int capacity;
    private final int refillRatePerTick;
    private int currentTokens;
    private long lastRefillTick;

    /**
     * Constructs a TokenBucketShaper.
     * 
     * @param capacity Maximum number of tokens the bucket can hold.
     * @param refillRatePerTick Tokens generated per simulation tick.
     * @param startTick The initial tick of the simulation.
     */
    public TokenBucketShaper(int capacity, int refillRatePerTick, long startTick) {
        if (capacity <= 0 || refillRatePerTick < 0) {
            throw new IllegalArgumentException("Capacity must be > 0 and refill rate >= 0");
        }
        this.capacity = capacity;
        this.refillRatePerTick = refillRatePerTick;
        this.currentTokens = capacity; // Start with a full bucket
        this.lastRefillTick = startTick;
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        if (currentTick < lastRefillTick) {
            // Time cannot move backward in a discrete-event simulation
            return false; 
        }

        long ticksPassed = currentTick - lastRefillTick;
        
        if (ticksPassed > 0) {
            // Use long to prevent integer overflow during calculation
            long tokensToAdd = ticksPassed * refillRatePerTick;
            long totalTokens = currentTokens + tokensToAdd;
            
            // Clamp tokens to the bucket's maximum capacity
            this.currentTokens = (int) Math.min(totalTokens, capacity);
            this.lastRefillTick = currentTick;
        }

        // Evaluate if enough tokens exist for the incoming packet
        if (this.currentTokens >= packet.sizeBytes()) {
            this.currentTokens -= packet.sizeBytes();
            return true;
        }

        // Not enough tokens; drop the packet
        return false;
    }
    
    public int getCurrentTokens() {
        return currentTokens;
    }
}