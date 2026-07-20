import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NetworkLink implements Tickable {
    private final int delayTicks;
    private final NetworkNode targetNode;
    private final Map<Long, List<Packet>> inTransitPackets;

    public NetworkLink(int delayTicks, NetworkNode targetNode) {
        if (delayTicks < 0) {
            throw new IllegalArgumentException("Delay ticks cannot be negative.");
        }
        this.delayTicks = delayTicks;
        this.targetNode = targetNode;
        this.inTransitPackets = new HashMap<>();
    }

    public void send(Packet p, long currentTick) {
        long arrivalTick = currentTick + delayTicks;
        // Schedule packet to arrive exactly at 'arrivalTick'
        inTransitPackets.computeIfAbsent(arrivalTick, k -> new ArrayList<>()).add(p);
    }

    @Override
    public void tick(long currentTick) {
        // Retrieve and remove packets due at the current tick
        List<Packet> arrivingPackets = inTransitPackets.remove(currentTick);
        
        if (arrivingPackets != null) {
            for (Packet p : arrivingPackets) {
                targetNode.receive(p, currentTick);
            }
        }
    }
}