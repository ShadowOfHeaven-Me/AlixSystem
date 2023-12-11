package shadow.utils.objects.packet.injector;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public final class ChannelInjectorProtocolLib implements ChannelInjector {

    @Override
    public Channel getChannel(Player player) throws Exception {
        return null;
    }

    @Override
    public String getProvider() {
        return "ProtocolLib";
    }
}