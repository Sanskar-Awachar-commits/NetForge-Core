import java.util.*;

public class MainDynamicMeshTest {
    record Node(String id, Map<String, Long> neighbors, Queue<Packet> buffer) {
        Node(String id) { this(id, new HashMap<>(), new ArrayDeque<>()); }
    }

    public static void main(String[] args) {
        Map<String, Node> mesh = new HashMap<>();
        for (String id : List.of("A", "B", "C", "D")) mesh.put(id, new Node(id));

        // Setup mesh links (target -> latency ticks)
        mesh.get("A").neighbors().put("B", 10L);
        mesh.get("A").neighbors().put("C", 25L);
        mesh.get("B").neighbors().put("D", 15L);
        mesh.get("C").neighbors().put("D", 10L);

        long currentTick = 0;
        Packet pkt = new Packet("PKT-001", 1024, currentTick, 1);

        // Route A -> D via primary path (A -> B -> D: total cost 25)
        String nextHop = findRoute(mesh, "A", "D", Set.of());
        System.out.printf("Tick %d: Primary route from A to D: %s%n", currentTick, nextHop);

        // Simulate link failure: sever link A -> B at tick 50
        currentTick = 50;
        Set<String> failedNodes = Set.of("B");
        String failoverHop = findRoute(mesh, "A", "D", failedNodes);
        System.out.printf("Tick %d: Self-healed route from A to D: %s%n", currentTick, failoverHop);

        mesh.get(failoverHop).buffer().offer(pkt);
        assert mesh.get("C").buffer().peek() != null : "Packet routing failed";
    }

    private static String findRoute(Map<String, Node> net, String src, String dst, Set<String> down) {
        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));
        Map<String, Long> dist = new HashMap<>();
        Map<String, String> firstHop = new HashMap<>();

        dist.put(src, 0L);
        for (var edge : net.get(src).neighbors().entrySet()) {
            if (!down.contains(edge.getKey())) {
                dist.put(edge.getKey(), edge.getValue());
                firstHop.put(edge.getKey(), edge.getKey());
                pq.offer(new long[]{edge.getKey().hashCode(), edge.getValue()});
            }
        }

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            String u = net.keySet().stream().filter(k -> k.hashCode() == cur[0]).findFirst().orElse("");
            if (u.equals(dst)) return firstHop.get(u);

            for (var edge : net.get(u).neighbors().entrySet()) {
                String v = edge.getKey();
                if (down.contains(v)) continue;
                long alt = dist.get(u) + edge.getValue();
                if (alt < dist.getOrDefault(v, Long.MAX_VALUE)) {
                    dist.put(v, alt);
                    firstHop.put(v, firstHop.get(u));
                    pq.offer(new long[]{v.hashCode(), alt});
                }
            }
        }
        return "UNREACHABLE";
    }
}