package shadow.utils.objects.packet.injector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import org.bukkit.entity.Player;
import shadow.utils.main.AlixHandler;

public interface ChannelInjector {

    Channel getChannel(Player player) throws Exception;

    String getProvider();
    
    default Channel inject(Player player, ChannelDuplexHandler handler, String handlerName) {
        try {
            Channel channel = this.getChannel(player);
            AlixHandler.inject(channel, handlerName, handler);
            return channel;
        } catch (Exception e) {
            player.kickPlayer("§cInvalid packet blocker. Report this as an error immediately!");
            throw new Error("An error occurred whilst trying to initialize the packet blocker! - Provider: " + this.getProvider(), e);
        }
    }
}