package shadow.utils.objects.filter.packet.check.ping.factory;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.objects.filter.packet.check.ping.PingCheck;
import shadow.utils.objects.filter.packet.types.PacketBlocker;

public interface PingCheckFactory {

    PingCheck createNew(Channel channel, Player player);

}