package src;
public record Packet(
    String id,
    int sizeBytes,
    long creationTick,
    int priority
) {}