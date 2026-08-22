import java.util.*;

public final class FailoverRouterNode {
    public interface DynamicRoutingTable {
        void remap(String flowPrefix, String interfaceId);
        String lookup(String flowPrefix);
    }

    public record LinkInterface(String id, boolean isOnline, int cost, QueuePolicy queue) {
        public boolean isDegraded() {
            return !isOnline || cost >= Integer.MAX_VALUE;
        }
    }

    private final DynamicRoutingTable routingTable;
    private final LinkInterface primaryLink;
    private final LinkInterface backupLink;
    private final Set<String> activeFlows;

    public FailoverRouterNode(DynamicRoutingTable table, LinkInterface primary, LinkInterface backup) {
        this.routingTable = Objects.requireNonNull(table);
        this.primaryLink = Objects.requireNonNull(primary);
        this.backupLink = Objects.requireNonNull(backup);
        this.activeFlows = new HashSet<>();
    }

    public void registerFlow(String flowPrefix) {
        activeFlows.add(flowPrefix);
        routingTable.remap(flowPrefix, primaryLink.id());
    }

    public void onTick(long currentTick) {
        String targetInterfaceId = primaryLink.isDegraded() ? backupLink.id() : primaryLink.id();
        for (String flow : activeFlows) {
            if (!targetInterfaceId.equals(routingTable.lookup(flow))) {
                routingTable.remap(flow, targetInterfaceId);
            }
        }
    }

    public boolean forward(String flowPrefix, Packet packet) {
        String targetId = routingTable.lookup(flowPrefix);
        LinkInterface active = targetId.equals(primaryLink.id()) ? primaryLink : backupLink;
        return !active.isDegraded() && active.queue().enqueue(packet);
    }
}