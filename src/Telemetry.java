/**
 * Encapsulates telemetry metrics collected by a network node (e.g. SinkNode).
 */
public record Telemetry(long processedCount, long duplicateCount, long droppedCount) {
    public long getProcessedCount() {
        return processedCount;
    }

    public long getDuplicateCount() {
        return duplicateCount;
    }

    public long getDroppedCount() {
        return droppedCount;
    }
}
