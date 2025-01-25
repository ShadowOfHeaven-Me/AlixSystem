package shadow.utils.misc.packets;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.play.PacketChatMessage;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class GroupPacket {

    //private static final GlobalCompressionHandler
    private final PacketOut packet;
    private final State.ProtocolMappings<PacketOut> mappings;

    private GroupPacket(PacketOut packet, State state) {
        this.packet = packet;
        this.mappings = state.clientBound;
    }

    public <T> void writeAndFlush(Collection<T> receivers, Function<T, Channel> channelGetter, Function<T, ClientVersion> versionGetter) {
        //there are usually 4 netty threads
        //Map<EventLoop, PooledList<Channel>> map = new IdentityHashMap<>(4);
        Map<ClientVersion, ByteBuf> cache = new HashMap(8, 1.0f);
        for (T receiver : receivers) {
            Channel channel = channelGetter.apply(receiver);
            ClientVersion ver = versionGetter.apply(receiver);

            Version version = Version.of(ver.getProtocolVersion());
            State.PacketRegistry registry = this.mappings.getRegistry(version);

            //PacketDuplexHandler.encodeToRaw0(this.packet, registry, version, )

            //channel.eventLoop().register()
        }
    }

    public static GroupPacket ofChatMessage(String message) {
        return new GroupPacket(PacketChatMessage.withMessage(message), State.PLAY);
    }
}