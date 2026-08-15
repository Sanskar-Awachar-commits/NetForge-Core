public interface TrafficShaper {
    boolean evaluate(Packet packet, long currentTick);
}