/*
package shadow.virtualization.handler;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.EventCreationUtil;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import shadow.Main;

public final class PacketHandler extends ChannelDuplexHandler {

    private final User user;

    public PacketHandler(Channel channel) {
        this.user = new User(channel, ConnectionState.HANDSHAKING, null, new UserProfile(null, null));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        int len = ByteBufHelper.readVarInt(buf);
        int packetId = ByteBufHelper.readVarInt(buf);

        PacketReceiveEvent event = EventCreationUtil.createReceiveEvent(ctx.channel(), user, null, buf, false);

        Main.logInfo("SEEEEEEX " + event.getPacketType());

        if (event.getPacketType() == PacketType.Handshaking.Client.HANDSHAKE) {
            WrapperHandshakingClientHandshake handshake = new WrapperHandshakingClientHandshake(event);
            ConnectionState nextState = handshake.getNextConnectionState();
            ClientVersion clientVersion = handshake.getClientVersion();
            user.setClientVersion(clientVersion);
            user.setConnectionState(nextState);
        } else if (event.getPacketType() == com.github.retrooper.packetevents.protocol.packettype.PacketType.Login.Client.LOGIN_SUCCESS_ACK) {
            user.setDecoderState(ConnectionState.CONFIGURATION);
        } else if (event.getPacketType() == com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client.CONFIGURATION_ACK) {
            user.setDecoderState(ConnectionState.CONFIGURATION);
        } else if (event.getPacketType() == com.github.retrooper.packetevents.protocol.packettype.PacketType.Configuration.Client.CONFIGURATION_END_ACK) {
            user.setDecoderState(ConnectionState.PLAY);
        }

    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }
}*/
