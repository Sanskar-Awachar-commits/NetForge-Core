public class BurstTrafficGenerator implements Tickable {
    private final String name;
    private final NetworkNode target;
    private final int burstSize;
    private final long intervalTicks;
    private final int packetSizeBytes;
    private long totalGenerated;

    public BurstTrafficGenerator(String name, int burstSize, int packetSizeBytes, long intervalTicks, NetworkNode target) {
        if (burstSize <= 0) {
            throw new IllegalArgumentException("Burst size must be strictly positive.");
        }
        this.name = name;
        this.target = target;
        this.burstSize = burstSize;
        this.intervalTicks = intervalTicks;
        this.packetSizeBytes = packetSizeBytes;
        this.totalGenerated = 0;
    }

    public BurstTrafficGenerator(String name, NetworkNode target) {
        this(name, 10, 256, 10, target);
    }

    public BurstTrafficGenerator(NetworkNode target, int burstSize, long intervalTicks) {
        this("BurstGenerator", burstSize, 256, intervalTicks, target);
    }

    public BurstTrafficGenerator(NetworkNode target, int burstSize, long intervalTicks, int packetSizeBytes) {
        this("BurstGenerator", burstSize, packetSizeBytes, intervalTicks, target);
    }

    public String getName() {
        return name;
    }

    @Override
    public void tick(long currentTick) {
        boolean shouldGenerate = false;
        if (intervalTicks <= 0) {
            if (totalGenerated == 0) {
                shouldGenerate = true;
            }
        } else if (currentTick % intervalTicks == 0) {
            shouldGenerate = true;
        }

        if (shouldGenerate) {
            for (int i = 0; i < burstSize; i++) {
                String packetId = "BURST-" + currentTick + "-" + (++totalGenerated);
                Packet packet = new Packet(packetId, packetSizeBytes, currentTick, 0);
                if (target != null) {
                    target.receivePacket(packet, currentTick);
                }
            }
        }
    }

    public long getTotalGenerated() {
        return totalGenerated;
    }
}