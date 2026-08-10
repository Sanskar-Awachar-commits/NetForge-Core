public class CorruptionShaper implements TrafficShaper {

    private static final int CORRUPTED_PRIORITY = -999;
    
    private final double corruptionRate;
    private final TrafficShaper innerShaper;

    /**
     * Constructs a CorruptionShaper to simulate random packet corruption.
     * 
     * @param corruptionRate The probability (0.0 to 1.0) of a packet being corrupted.
     * @param innerShaper    The next shaper in the chain to evaluate the packet.
     */
    public CorruptionShaper(double corruptionRate, TrafficShaper innerShaper) {
        if (corruptionRate < 0.0 || corruptionRate > 1.0) {
            throw new IllegalArgumentException("Corruption rate must be between 0.0 and 1.0");
        }
        this.corruptionRate = corruptionRate;
        this.innerShaper = innerShaper;
    }

    @Override
    public boolean evaluate(Packet packet, long currentTick) {
        Packet packetToForward = packet;

        if (Math.random() < corruptionRate) {
            packetToForward = new Packet(
                packet.id(),
                packet.sizeBytes(),
                packet.creationTick(),
                CORRUPTED_PRIORITY
            );
            System.out.println("Packet " + packet.id() + " corrupted");
        }

        if (innerShaper != null) {
            return innerShaper.evaluate(packetToForward, currentTick);
        }
        
        return true;
    }
}