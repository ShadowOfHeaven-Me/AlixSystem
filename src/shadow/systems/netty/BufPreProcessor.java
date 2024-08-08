/*
package shadow.systems.netty;

import alix.common.antibot.firewall.FireWallManager;
import alix.common.utils.other.AlixUnsafe;
import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import shadow.Main;
import sun.misc.Unsafe;

import java.net.InetSocketAddress;

public final class BufPreProcessor extends ChannelDuplexHandler {

    private static final Unsafe UNSAFE = AlixUnsafe.getUnsafe();
    public static final String BUF_PRE_PROCESSOR_NAME = "alix-buf-preprocessor";

    private final User user;

    public BufPreProcessor(ChannelPipeline pipeline) {
        this.user = ProtocolManager.USERS.get(pipeline);
        if (user == null) throw new AlixException("No user - BufPreProcessor!");
        //Main.logError("USER: " + this.user);
    }

*/
/*    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Main.logError("READ BUF: " + msg);
        super.channelRead(ctx, msg);
    }*//*


*/
/*    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }*//*


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        int readerIndex = buf.readerIndex();

        //https://wiki.vg/Protocol#Without_compression
        int length = readVarInt(buf);
        int packetId = readVarInt(buf);

        Main.logError("PACKET ID: " + packetId + " LENGTH " + length + " " + readerIndex);

        */
/*if (length == -3 || packetId == -3) {
            //firewall(ctx);
            buf.release();
            return;
        }*//*


        PacketTypeCommon type = PacketType.getById(PacketSide.CLIENT, this.user.getDecoderState(), this.user.getClientVersion(), packetId);

        Main.logError("PACKET TYPE: " + type.getName());

        */
/*if (type == null) {
            //firewall(ctx);
            ctx.channel().close();
            buf.release();
            return;
        }

        if (type == PacketType.Handshaking.Client.HANDSHAKE) {
            WrapperHandshakingClientHandshake handshake = (WrapperHandshakingClientHandshake) UNSAFE.allocateInstance(WrapperHandshakingClientHandshake.class);
            handshake.buffer = buf;
            try {
                handshake.read();
                Main.logError("VALID HANDSHAKE :]]]]]");
            } catch (InvalidHandshakeException e) {
                Main.logError("INVALID HANDSHAKE");
                //firewall(ctx);
                ctx.channel().close();
                buf.release();
                return;
            }
        } else if (type == PacketType.Login.Client.LOGIN_START) {
            Main.logError("ON LOGIN START");
            if (AlixChannelHandler.onLoginStart(buf, this.user)) return;
            ctx.pipeline().remove(BUF_PRE_PROCESSOR_NAME);
            ctx.pipeline().remove(CHANNEL_MONITOR_NAME);
        }*//*


        buf.readerIndex(readerIndex);
        super.channelRead(ctx, msg);
    }

*/
/*    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        AlixChannelHandler.removeFromTimeOut(ctx.channel());//important fix
        super.close(ctx, promise);
    }*//*


*/
/*    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Main.logError("REGISTERED");
        super.channelRegistered(ctx);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Main.logError("ACTIVE in processor");
        super.channelActive(ctx);
    }*//*


*/
/*    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Main.logError("ERRORRRRR " + cause.getMessage());
        cause.printStackTrace();
        ctx.channel().close();
        //super.exceptionCaught(ctx, cause);
    }*//*


  */
/*  @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Main.logError("ACTIVE " + ctx.name());


        TIMEOUT_TASKS.put(channel, channel.eventLoop().schedule(() -> {
            TIMEOUT_TASKS.remove(channel);
            NettyUtils.closeAfterConstSend(channel, timeOutError);
        }, 3, TimeUnit.SECONDS));
        super.channelActive(ctx);
    }*//*


    @Override
    public boolean isSharable() {
        return false;
    }

    private static void firewall(ChannelHandlerContext ctx) {
        FireWallManager.addCauseException(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
        ctx.channel().close();
    }

    private static int readVarInt(ByteBuf buffer) {
        int value = 0;
        int length = 0;

        byte currentByte;
        do {
            if (!buffer.isReadable()) return -3;//the VarInt is invalid
            currentByte = buffer.readByte();
            value |= (currentByte & 127) << length * 7;
            if (++length > 5) return -3;//the VarInt is too large
        } while ((currentByte & 128) == 128);

        return value;
    }

//    @Override
//    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
//        Main.logError("READ BUF: " + byteBuf);
//        list.add(byteBuf.retain());
//    }
}*/
