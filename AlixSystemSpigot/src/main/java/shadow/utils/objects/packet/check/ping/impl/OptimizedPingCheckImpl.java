/*
package shadow.utils.objects.packet.check.ping.impl;

import io.netty.channel.Channel;
import net.minecraft.network.protocol.game.PacketPlayInKeepAlive;
import org.bukkit.entity.Player;
import shadow.utils.holders.ReflectionUtils;

public class OptimizedPingCheckImpl extends AbstractPingCheck {

    public OptimizedPingCheckImpl(Channel channel, Player player) {
        super(channel, player);
    }

*/
/*    @Override
    protected long getId(Object packet) {
        return ((PacketPlayInKeepAlive) packet).b();
    }*//*


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
        return new PacketPlayInKeepAlive(id);
    }
}*/
