package shadow.utils.objects.packet.check.ping.factory;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.objects.packet.check.ping.PingCheck;

public interface PingCheckFactory {

    PingCheck createNew(Channel channel, Player player);

}