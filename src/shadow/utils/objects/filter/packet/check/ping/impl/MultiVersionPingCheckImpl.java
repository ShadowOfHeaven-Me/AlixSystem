package shadow.utils.objects.filter.packet.check.ping.impl;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.holders.ReflectionUtils;

public class MultiVersionPingCheckImpl extends AbstractPingCheck {

    public MultiVersionPingCheckImpl(Channel channel, Player player) {
        super(channel, player);
    }

    @Override
    protected long getId(Object packet) {
        try {
            return (long) ReflectionUtils.getKeepAliveMethod.invoke(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object createPacket(long id) {
        try {
            return ReflectionUtils.outKeepAliveConstructor.newInstance(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}