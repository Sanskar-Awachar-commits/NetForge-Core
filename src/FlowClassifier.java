import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlowClassifier implements TrafficShaper {
    private final Map<String, Integer> ruleTable;
    private final Map<String, Long> flowCounters;
    private Packet lastClassifiedPacket;

    public FlowClassifier(Map<String, Integer> rules) {
        this.ruleTable = new HashMap<>(rules);
        this.flowCounters = new HashMap<>();
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        if (packet == null) {
            return false;
        }

        int priority = packet.priority();
        String matchedPrefix = null;

        for (Map.Entry<String, Integer> entry : ruleTable.entrySet()) {
            if (packet.id().startsWith(entry.getKey())) {
                priority = entry.getValue();
                matchedPrefix = entry.getKey();
                break;
            }
        }

        if (matchedPrefix != null) {
            flowCounters.merge(matchedPrefix, 1L, Long::sum);
            this.lastClassifiedPacket = new Packet(
                packet.id(),
                packet.sizeBytes(),
                packet.creationTick(),
                priority
            );
        } else {
            this.lastClassifiedPacket = packet;
        }

        return true;
    }

    public Packet getLastClassifiedPacket() {
        return lastClassifiedPacket;
    }

    public Map<String, Long> getFlowCounters() {
        return Collections.unmodifiableMap(flowCounters);
    }
}