package shadow.utils.objects.packet.injector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public final class ChannelInjectorPacketEvents implements ChannelInjector {

    private final ProtocolManager protocolManager = PacketEvents.getAPI().getProtocolManager();

    @Override
    public Channel getChannel(Player player) {
        return (Channel) protocolManager.getChannel(player.getUniqueId());
    }

    @Override
    public String getProvider() {
        return "PacketEvents";
    }
}