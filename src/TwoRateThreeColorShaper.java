import java.util.Objects;

/**
 * TwoRateThreeColorShaper implements RFC 2698 Two-Rate Three-Color Marker (trTCM)
 * metering for traffic policing.
 */
public class TwoRateThreeColorShaper implements TrafficShaper {
    public static final int RED_PRIORITY = 1;
    public static final int YELLOW_PRIORITY = 2;
    public static final int GREEN_PRIORITY = 3;

    private final int peakRateBytesPerTick;
    private final int peakBurstBytes;
    private final int committedRateBytesPerTick;
    private final int committedBurstBytes;
    private final TrafficShaper innerShaper;

    private int peakTokens;
    private int committedTokens;
    private long lastRefillTick;

    public TwoRateThreeColorShaper(int peakRateBytesPerTick, int peakBurstBytes,
                                  int committedRateBytesPerTick, int committedBurstBytes) {
        this(peakRateBytesPerTick, peakBurstBytes, committedRateBytesPerTick, committedBurstBytes, null);
    }

    public TwoRateThreeColorShaper(int peakRateBytesPerTick, int peakBurstBytes,
                                  int committedRateBytesPerTick, int committedBurstBytes,
                                  TrafficShaper innerShaper) {
        if (peakRateBytesPerTick < 0 || peakBurstBytes <= 0 || committedRateBytesPerTick < 0 || committedBurstBytes <= 0) {
            throw new IllegalArgumentException("Rates must be >= 0 and burst sizes must be > 0");
        }
        this.peakRateBytesPerTick = peakRateBytesPerTick;
        this.peakBurstBytes = peakBurstBytes;
        this.committedRateBytesPerTick = committedRateBytesPerTick;
        this.committedBurstBytes = committedBurstBytes;
        this.peakTokens = peakBurstBytes;
        this.committedTokens = committedBurstBytes;
        this.lastRefillTick = 0L;
        this.innerShaper = innerShaper;
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        if (currentTick < lastRefillTick) return false;

        long elapsedTicks = currentTick - lastRefillTick;
        if (elapsedTicks > 0) {
            long newPeak = (long) peakTokens + (elapsedTicks * peakRateBytesPerTick);
            peakTokens = (int) Math.min(newPeak, peakBurstBytes);

            long newCommitted = (long) committedTokens + (elapsedTicks * committedRateBytesPerTick);
            committedTokens = (int) Math.min(newCommitted, committedBurstBytes);

            lastRefillTick = currentTick;
        }

        int packetSize = packet.sizeBytes();

        if (packetSize > peakTokens) {
            Packet red = new Packet(packet.id(), packet.sizeBytes(), packet.creationTick(), RED_PRIORITY);
            if (innerShaper != null) innerShaper.evaluate(red, currentTick);
            return false;
        } else if (packetSize > committedTokens) {
            peakTokens -= packetSize;
            Packet yellow = new Packet(packet.id(), packet.sizeBytes(), packet.creationTick(), YELLOW_PRIORITY);
            return innerShaper != null ? innerShaper.evaluate(yellow, currentTick) : true;
        } else {
            peakTokens -= packetSize;
            committedTokens -= packetSize;
            Packet green = new Packet(packet.id(), packet.sizeBytes(), packet.creationTick(), GREEN_PRIORITY);
            return innerShaper != null ? innerShaper.evaluate(green, currentTick) : true;
        }
    }

    public int getPeakTokens() { return peakTokens; }
    public int getCommittedTokens() { return committedTokens; }
}
