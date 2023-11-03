package shadow.utils.objects.filter.packet.injector;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.holders.ReflectionUtils;

public final class ChannelInjectorNMS implements ChannelInjector {

    @Override
    public Channel getChannel(Player p) throws Exception {
        Object entityPlayer = ReflectionUtils.getHandle.invoke(p);
        Object playerConnection = ReflectionUtils.getPlayerConnection.get(entityPlayer);
        Object networkManager = ReflectionUtils.getNetworkManager.get(playerConnection);
        return (Channel) ReflectionUtils.getChannel.get(networkManager);
    }

    @Override
    public String getProvider() {
        return "NMS";
    }
}