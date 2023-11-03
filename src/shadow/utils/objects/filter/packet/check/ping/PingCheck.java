package shadow.utils.objects.filter.packet.check.ping;

public interface PingCheck {

    //Returns: whether the server can process the packet
    boolean receiveKeepAlive(Object keepAlivePacket);

    boolean isCompleted();

}