import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JitteryNetworkLink implements Tickable {
    private final int baseDelayTicks;
    private final int maxJitterTicks;
    private final NetworkNode targetNode;
    private final Map<Long, List<Packet>> scheduledPackets = new HashMap<>();

    public JitteryNetworkLink(int baseDelayTicks, int maxJitterTicks, NetworkNode targetNode) {
        this.baseDelayTicks = baseDelayTicks;
        this.maxJitterTicks = maxJitterTicks;
        this.targetNode = targetNode;
    }

    public void send(Packet p, long currentTick) {
        int jitter = maxJitterTicks > 0 ? (int) (Math.random() * maxJitterTicks) : 0;
        long scheduledTick = currentTick + baseDelayTicks + jitter;
        scheduledPackets.computeIfAbsent(scheduledTick, k -> new ArrayList<>()).add(p);
    }

    @Override
    public void tick(long currentTick) {
        List<Long> dueTicks = new ArrayList<>();
        
        for (Long tick : scheduledPackets.keySet()) {
            if (tick <= currentTick) {
                dueTicks.add(tick);
            }
        }

        for (Long dueTick : dueTicks) {
            List<Packet> packets = scheduledPackets.remove(dueTick);
            if (packets != null) {
                for (Packet packet : packets) {
                    targetNode.receive(packet, currentTick);
                }
            }
        }
    }
}