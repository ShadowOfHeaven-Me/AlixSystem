/*
package shadow.utils.objects.packet.injector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import org.bukkit.entity.Player;

public interface ChannelInjector {

    //Object errorKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase("§cInvalid packet blocker. Report this as an error immediately!");

    Channel getChannel(Player player) throws Exception;

    String getProvider();

    default void inject(Channel channel, ChannelDuplexHandler handler, String handlerName) {
        channel.pipeline().addBefore("packet_handler", handlerName, handler);
    }
            */
/*try {
            channel.pipeline().addFirst(handlerName, handler);
            //channel.pipeline().addBefore("packet_handler", handlerName, handler);

        } catch (Exception e) {
            //MethodProvider.kickAsync(channel, errorKickPacket);
            //player.kickPlayer("§cInvalid packet blocker. Report this as an error immediately!");
            throw new Error("An error occurred whilst trying to initialize the packet blocker! - Provider: " + this.getProvider(), e);
        }*//*

}*/
