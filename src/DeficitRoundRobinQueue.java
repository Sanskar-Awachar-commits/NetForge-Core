import java.util.*;

public class DeficitRoundRobinQueue implements QueuePolicy {
    private final int maxCapacity;
    private final int quantumBytes;
    private final Map<Integer, Queue<Packet>> queues = new HashMap<>();
    private final Map<Integer, Integer> deficitCounters = new HashMap<>();
    private final List<Integer> priorities = new ArrayList<>();
    private int currentPriorityIdx = 0;
    private boolean newRoundForCurrentQueue = true;
    private int totalPackets = 0;

    public DeficitRoundRobinQueue(int maxCapacity, int quantumBytes) {
        this.maxCapacity = maxCapacity;
        this.quantumBytes = quantumBytes;
    }

    public DeficitRoundRobinQueue(int maxCapacity) {
        this(maxCapacity, 512);
    }

    @Override
    public synchronized boolean enqueue(Packet packet) {
        if (totalPackets >= maxCapacity) {
            return false;
        }
        Queue<Packet> queue = queues.computeIfAbsent(packet.priority(), p -> {
            priorities.add(p);
            deficitCounters.put(p, 0);
            return new ArrayDeque<>();
        });
        queue.offer(packet);
        totalPackets++;
        return true;
    }

    @Override
    public synchronized Optional<Packet> dequeue() {
        if (totalPackets == 0 || priorities.isEmpty()) {
            return Optional.empty();
        }

        while (totalPackets > 0) {
            int priority = priorities.get(currentPriorityIdx);
            Queue<Packet> queue = queues.get(priority);

            if (queue == null || queue.isEmpty()) {
                deficitCounters.put(priority, 0);
                currentPriorityIdx = (currentPriorityIdx + 1) % priorities.size();
                newRoundForCurrentQueue = true;
                continue;
            }

            if (newRoundForCurrentQueue) {
                int currentDeficit = deficitCounters.getOrDefault(priority, 0);
                deficitCounters.put(priority, currentDeficit + quantumBytes);
                newRoundForCurrentQueue = false;
            }

            int deficit = deficitCounters.getOrDefault(priority, 0);
            Packet head = queue.peek();

            if (head != null && head.sizeBytes() <= deficit) {
                deficitCounters.put(priority, deficit - head.sizeBytes());
                queue.poll();
                totalPackets--;

                if (queue.isEmpty()) {
                    deficitCounters.put(priority, 0);
                    currentPriorityIdx = (currentPriorityIdx + 1) % priorities.size();
                    newRoundForCurrentQueue = true;
                }
                return Optional.of(head);
            } else {
                currentPriorityIdx = (currentPriorityIdx + 1) % priorities.size();
                newRoundForCurrentQueue = true;
            }
        }

        return Optional.empty();
    }

    @Override
    public synchronized int currentSize() {
        return totalPackets;
    }
}
