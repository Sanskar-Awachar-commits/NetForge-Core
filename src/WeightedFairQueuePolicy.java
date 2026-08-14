import java.util.*;

public class WeightedFairQueuePolicy implements QueuePolicy {
    private final int maxCapacity;
    private final Map<Integer, Integer> weights;
    private final Map<Integer, Queue<Packet>> queues = new HashMap<>();
    private final List<Integer> priorities = new ArrayList<>();
    private int currentPriorityIdx = 0;
    private int currentServedCount = 0;
    private int totalSize = 0;

    public WeightedFairQueuePolicy(int maxCapacity, Map<Integer, Integer> priorityWeights) {
        this.maxCapacity = maxCapacity;
        this.weights = new HashMap<>(priorityWeights);
        for (int p : priorityWeights.keySet()) {
            queues.put(p, new ArrayDeque<>());
            priorities.add(p);
        }
        priorities.sort(Collections.reverseOrder());
    }

    @Override
    public synchronized boolean enqueue(Packet packet) {
        if (totalSize >= maxCapacity) {
            return false;
        }
        Queue<Packet> queue = queues.computeIfAbsent(packet.priority(), p -> {
            weights.putIfAbsent(p, Math.max(1, p));
            priorities.add(p);
            priorities.sort(Collections.reverseOrder());
            return new ArrayDeque<>();
        });
        queue.offer(packet);
        totalSize++;
        return true;
    }

    @Override
    public synchronized Optional<Packet> dequeue() {
        if (totalSize == 0 || priorities.isEmpty()) {
            return Optional.empty();
        }

        int attempts = 0;
        while (attempts < priorities.size()) {
            int priority = priorities.get(currentPriorityIdx);
            int quota = weights.getOrDefault(priority, 1);
            Queue<Packet> queue = queues.get(priority);

            if (queue != null && !queue.isEmpty()) {
                Packet packet = queue.poll();
                totalSize--;
                currentServedCount++;

                if (currentServedCount >= quota || queue.isEmpty()) {
                    currentServedCount = 0;
                    currentPriorityIdx = (currentPriorityIdx + 1) % priorities.size();
                }
                return Optional.of(packet);
            }

            currentServedCount = 0;
            currentPriorityIdx = (currentPriorityIdx + 1) % priorities.size();
            attempts++;
        }

        return Optional.empty();
    }

    @Override
    public synchronized int currentSize() {
        return totalSize;
    }
}