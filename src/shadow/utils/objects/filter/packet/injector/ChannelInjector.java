package shadow.utils.objects.filter.packet.injector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import org.bukkit.entity.Player;
import shadow.utils.objects.filter.packet.types.PacketBlocker;

public interface ChannelInjector {

    Channel getChannel(Player player) throws Exception;

    String getProvider();

    default Channel inject(Player player, ChannelDuplexHandler handler) {
        try {
            Channel channel = this.getChannel(player);
            channel.pipeline().addBefore("packet_handler", PacketBlocker.packetHandlerName, handler);
            return channel;
        } catch (Exception e) {
            player.kickPlayer("§cInvalid packet blocker. Report this as an error immediately!");
            throw new Error("An error occurred whilst trying to initialize the packet blocker! - Provider: " + this.getProvider(), e);
        }
    }
}