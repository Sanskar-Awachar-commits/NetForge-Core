import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JitteryNetworkLink extends NetworkLink {
    private final int baseDelayTicks;
    private final int maxJitterTicks;
    private final NetworkNode targetNode;
    private final Map<Long, List<Packet>> scheduledPackets = new HashMap<>();

    public JitteryNetworkLink(String name, NetworkNode targetNode) {
        this(name, 2, 2, targetNode);
    }

    public JitteryNetworkLink(int baseDelayTicks, int maxJitterTicks, NetworkNode targetNode) {
        this("JitteryNetworkLink", baseDelayTicks, maxJitterTicks, targetNode);
    }

    public JitteryNetworkLink(String name, int baseDelayTicks, int maxJitterTicks, NetworkNode targetNode) {
        super(name, baseDelayTicks, targetNode);
        this.baseDelayTicks = baseDelayTicks;
        this.maxJitterTicks = maxJitterTicks;
        this.targetNode = targetNode;
    }

    @Override
    public void send(Packet p, long currentTick) {
        if (p == null) {
            return;
        }
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
                    if (targetNode != null) {
                        targetNode.receivePacket(packet, currentTick);
                    }
                }
            }
        }
    }
}