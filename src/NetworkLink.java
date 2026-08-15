import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkLink extends NetworkNode {
    private final int delayTicks;
    private final NetworkNode targetNode;
    private final Map<Long, List<Packet>> inTransitPackets;

    public NetworkLink(int delayTicks, NetworkNode targetNode) {
        this("NetworkLink", delayTicks, targetNode);
    }

    public NetworkLink(String name, NetworkNode targetNode) {
        this(name, 1, targetNode);
    }

    public NetworkLink(String name, int delayTicks, NetworkNode targetNode) {
        super(name);
        if (delayTicks < 0) {
            throw new IllegalArgumentException("Delay ticks cannot be negative.");
        }
        this.delayTicks = delayTicks;
        this.targetNode = targetNode;
        this.inTransitPackets = new HashMap<>();
    }

    public NetworkNode getTargetNode() {
        return targetNode;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public void send(Packet p, long currentTick) {
        if (p == null) {
            return;
        }
        long arrivalTick = currentTick + delayTicks;
        inTransitPackets.computeIfAbsent(arrivalTick, k -> new ArrayList<>()).add(p);
    }

    @Override
    public boolean receive(Packet p) {
        send(p, 0L);
        return true;
    }

    @Override
    public boolean receive(Packet p, long currentTick) {
        send(p, currentTick);
        return true;
    }

    @Override
    public boolean receivePacket(Packet p) {
        send(p, 0L);
        return true;
    }

    @Override
    public boolean receivePacket(Packet p, long currentTick) {
        send(p, currentTick);
        return true;
    }

    @Override
    public void tick(long currentTick) {
        List<Packet> arrivingPackets = inTransitPackets.remove(currentTick);
        if (arrivingPackets != null) {
            for (Packet p : arrivingPackets) {
                if (targetNode != null) {
                    targetNode.receivePacket(p, currentTick);
                }
            }
        }
    }
}