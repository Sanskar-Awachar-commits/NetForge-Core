public class BurstTrafficGenerator implements Tickable {
    private final NetworkNode target;
    private final int burstSize;
    private final long intervalTicks;
    private final int packetSizeBytes;
    private long totalGenerated;

    public BurstTrafficGenerator(NetworkNode target, int burstSize, long intervalTicks, int packetSizeBytes) {
        if (intervalTicks <= 0 || burstSize <= 0) {
            throw new IllegalArgumentException("Interval and burst size must be strictly positive.");
        }
        this.target = target;
        this.burstSize = burstSize;
        this.intervalTicks = intervalTicks;
        this.packetSizeBytes = packetSizeBytes;
        this.totalGenerated = 0;
    }

    @Override
    public void tick(long currentTick) {
        
        if (currentTick % intervalTicks == 0) {
            for (int i = 0; i < burstSize; i++) {
                String packetId = "BURST-" + currentTick + "-" + (++totalGenerated);
                
                Packet packet = new Packet(packetId, packetSizeBytes, currentTick, 0);
                target.receivePacket(packet);
            }
        }
    }

    public long getTotalGenerated() {
        return totalGenerated;
    }
}