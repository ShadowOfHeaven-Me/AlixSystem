package shadow.utils.objects.filter.packet.check.ping.factory.impl;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.objects.filter.packet.check.ping.PingCheck;
import shadow.utils.objects.filter.packet.check.ping.factory.PingCheckFactory;
import shadow.utils.objects.filter.packet.check.ping.impl.MultiVersionPingCheckImpl;
import shadow.utils.objects.filter.packet.types.PacketBlocker;

public final class MultiVersionPingCheckFactoryImpl implements PingCheckFactory {

    @Override
    public PingCheck createNew(Channel channel, Player player) {
        return new MultiVersionPingCheckImpl(channel, player);
    }
}
