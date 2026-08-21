import java.util.*;
import java.util.function.Consumer;

interface Tickable {
    void tick(long currentTick);
}

public class LsaFloodAgent implements Tickable {
    private final String nodeId;
    private final Set<String> processedLsaIds = new HashSet<>();
    private final Map<String, Consumer<Packet>> outboundInterfaces = new HashMap<>();
    private final Consumer<Packet> graphSnapshotUpdater;

    public LsaFloodAgent(String nodeId, Consumer<Packet> graphSnapshotUpdater) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.graphSnapshotUpdater = Objects.requireNonNull(graphSnapshotUpdater);
    }

    public void registerInterface(String interfaceId, Consumer<Packet> transmitter) {
        outboundInterfaces.put(interfaceId, transmitter);
    }

    public boolean receiveLsa(Packet lsaPacket, String ingressInterfaceId) {
        if (lsaPacket == null || !processedLsaIds.add(lsaPacket.id())) {
            return false;
        }

        graphSnapshotUpdater.accept(lsaPacket);

        for (Map.Entry<String, Consumer<Packet>> entry : outboundInterfaces.entrySet()) {
            if (!entry.getKey().equals(ingressInterfaceId)) {
                Packet cloned = new Packet(
                    lsaPacket.id(),
                    lsaPacket.sizeBytes(),
                    lsaPacket.creationTick(),
                    lsaPacket.priority()
                );
                entry.getValue().accept(cloned);
            }
        }
        return true;
    }

    @Override
    public void tick(long currentTick) {
        // Hook for scheduled state aging and periodic advertisement sweeps
    }

    public Set<String> getProcessedLsaIds() {
        return Collections.unmodifiableSet(processedLsaIds);
    }
}