package netforge.core.shaping;

import java.util.Map;
import java.util.Objects;

/**
 * Hierarchical Token Bucket (HTB) traffic shaper supporting multi-tenant
 * bandwidth isolation and parent token borrowing for excess capacity.
 */
public final class HierarchicalTokenBucket implements TrafficShaper {
    private final TrafficShaper parentShaper;
    private final Map<Integer, TrafficShaper> childShapers;
    private final TrafficShaper parentBorrowShaper;

    /**
     * @param parentShaper Total physical pipe rate limiter
     * @param childShapers Tenant rate limiters mapped by packet priority
     * @param parentBorrowShaper Evaluator ensuring parent retains reserve threshold when borrowing
     */
    public HierarchicalTokenBucket(
            TrafficShaper parentShaper,
            Map<Integer, TrafficShaper> childShapers,
            TrafficShaper parentBorrowShaper) {
        this.parentShaper = Objects.requireNonNull(parentShaper, "parentShaper cannot be null");
        this.childShapers = Map.copyOf(Objects.requireNonNull(childShapers, "childShapers cannot be null"));
        this.parentBorrowShaper = Objects.requireNonNull(parentBorrowShaper, "parentBorrowShaper cannot be null");
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        Objects.requireNonNull(packet, "packet cannot be null");
        TrafficShaper child = childShapers.get(packet.priority());
        
        if (child == null) {
            return false;
        }

        boolean childAllowed = child.evaluate(packet, currentTick);
        if (childAllowed) {
            return parentShaper.evaluate(packet, currentTick);
        }

        // Child quota exhausted: check and borrow excess tokens from parent above reserve threshold
        return parentBorrowShaper.evaluate(packet, currentTick);
    }
}