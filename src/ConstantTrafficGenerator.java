package src;
import java.util.UUID;

public class ConstantTrafficGenerator implements Tickable {

    private final NetworkNode target;
    private final int packetSize;
    private final int intervalTicks;

    public ConstantTrafficGenerator(NetworkNode target, int packetSize, int intervalTicks) {
        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("Simulation error: intervalTicks must be greater than 0.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Simulation error: Target NetworkNode cannot be null.");
        }
        
        this.target = target;
        this.packetSize = packetSize;
        this.intervalTicks = intervalTicks;
    }

    @Override
    public void tick(long currentTick) {
        if (currentTick % intervalTicks == 0) {
            Packet packet = new Packet(
                UUID.randomUUID().toString(),
                packetSize,
                currentTick,
                0
            );
            
            target.receivePacket(packet);
        }
    }
}