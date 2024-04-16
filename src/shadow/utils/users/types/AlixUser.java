package shadow.utils.users.types;

import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.objects.packet.PacketProcessor;

public interface AlixUser {

    User reetrooperUser();

    PacketProcessor getPacketProcessor();

    boolean isVerified();

    Channel getChannel();

    ChannelHandlerContext silentContext();

}