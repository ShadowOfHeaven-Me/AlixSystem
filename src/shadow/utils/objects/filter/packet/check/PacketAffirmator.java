package shadow.utils.objects.filter.packet.check;

public interface PacketAffirmator {

    boolean isWindowOrKeepAlive(Object packet);

    boolean isCommandOrKeepAlive(Object packet);

    boolean isKeepAlive(Object packet);

    boolean isCommand(Object packet);

    boolean isMove(Object packet);

    boolean isKeepAlive(String name);

    boolean isCommand(String name);

    boolean isMove(String name);

}