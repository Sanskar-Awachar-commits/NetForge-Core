import java.util.*;

public class DiagnosticDropLogger implements TrafficShaper {
    public enum DropReason { BUFFER_OVERFLOW, RATE_EXCEEDED, FIREWALL_BLOCKED, CORRUPTED, TTL_EXPIRED }

    private static final int MAX_LOG_SIZE = 20;
    private final TrafficShaper innerShaper;
    private final String componentName;
    private final Map<DropReason, Long> dropCounters = new EnumMap<>(DropReason.class);
    private final List<String> recentDropLog = new ArrayList<>(MAX_LOG_SIZE);

    public DiagnosticDropLogger(TrafficShaper innerShaper, String componentName) {
        this.innerShaper = Objects.requireNonNull(innerShaper);
        this.componentName = Objects.requireNonNull(componentName);
        for (DropReason reason : DropReason.values()) {
            dropCounters.put(reason, 0L);
        }
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        boolean accepted = innerShaper.evaluate(packet, currentTick);
        if (!accepted) {
            recordDrop(classifyDrop(packet), packet, currentTick);
        }
        return accepted;
    }

    private DropReason classifyDrop(Packet packet) {
        return DropReason.RATE_EXCEEDED;
    }

    private void recordDrop(DropReason reason, Packet packet, long tick) {
        dropCounters.merge(reason, 1L, Long::sum);
        if (recentDropLog.size() >= MAX_LOG_SIZE) {
            recentDropLog.remove(0);
        }
        recentDropLog.add(String.format("[Tick %d][%s] Dropped packet %s (size: %dB, prio: %d) -> %s",
                tick, componentName, packet.id(), packet.sizeBytes(), packet.priority(), reason));
    }

    public void printDropReport() {
        System.out.printf("--- Drop Report: %s ---%n", componentName);
        dropCounters.forEach((reason, count) -> System.out.printf("  %s: %d%n", reason, count));
        System.out.println("Recent Drops:");
        recentDropLog.forEach(log -> System.out.printf("  %s%n", log));
    }

    public Map<DropReason, Long> getDropCounters() { return Collections.unmodifiableMap(dropCounters); }
    public List<String> getRecentDropLog() { return Collections.unmodifiableList(recentDropLog); }
}