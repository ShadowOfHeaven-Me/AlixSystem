package shadow.utils.objects.packet.check.ping;

public interface PingCheck {

    //Returns: whether the server can process the packet
    boolean receiveKeepAlive(Object keepAlivePacket);

    boolean isCompleted();

}