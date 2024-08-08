package shadow.systems.login.firewall;

import alix.common.messages.Messages;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import shadow.utils.misc.packet.constructors.OutDisconnectKickPacketConstructor;

@Sharable
public final class DelayedChannelFireWall extends ChannelDuplexHandler {

    public static final DelayedChannelFireWall INSTANCE = new DelayedChannelFireWall();//the interceptor's proxy (it's necessary to wait for channelActive)
    private static final String interceptorName = "alix_dfw_inter";//Alix Delayed FireWall Interceptor
    private final DelayedInterceptor interceptor = new DelayedInterceptor();//the actual packet interceptor
    //private static final String interceptorName = "alixsystem_firewall";
    //private final boolean fastRaw = Main.config.getBoolean("fast-raw-firewall");

    private DelayedChannelFireWall() {
    }
/*
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Main.logError("REGISTERED: " + ctx);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Main.logError("UNREGISTERED: " + ctx);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Main.logError("INACTIVE: " + ctx);
        super.channelInactive(ctx);
    }*/

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //Main.logError("ACTIVE: " + ctx);
        //ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("'mimimimimxxxw'".getBytes()));

        //ctx.close();
        //Main.logError("ACTIVEEE ");
        super.channelActive(ctx);
        //Main.logError("ACTIVEEE 22222222222");
        ctx.channel().pipeline().addBefore("packet_handler", interceptorName, this.interceptor);
    }
/*
    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Main.logError("BYTE BUF READ " + msg.getClass().getSimpleName() + " msg: " + msg + " ctx: " + ctx);
        *//*if (msg.getClass() == ReflectionUtils.loginInStartPacketClass)
            ctx.channel().writeAndFlush(kickPacket).addListener(ChannelFutureListener.CLOSE);
        else *//*
        super.channelRead(ctx, msg);
    }*/

    @Sharable
    private static final class DelayedInterceptor extends ChannelDuplexHandler {

        //private static final ChannelFutureListener CLOSE_FORCIBLY = future -> future.channel().unsafe().closeForcibly();
        private final Object kickPacket = OutDisconnectKickPacketConstructor.constructConstAtLoginPhase(Messages.get("anti-bot-firewalled"));

        //TODO: This was included \/
        /*@Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //Main.logError("REAAADD: " + msg.getClass().getSimpleName());
            if (msg.getClass() == ReflectionUtils.loginInStartPacketClass)
                ctx.channel().writeAndFlush(kickPacket).addListener(ChannelFutureListener.CLOSE);
            else super.channelRead(ctx, msg);
        }*/

        ///212.88.124.174:58009 lost connection: Internal Exception: io.netty.handler.codec.DecoderException: java.lang.IndexOutOfBoundsException: readerIndex(18) + length(8) exceeds writerIndex(19): PooledUnsafeDirectByteBuf(ridx: 18, widx: 19, cap: 256)
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            //if(cause.getCause().getClass() == DecoderException.class)
            //FireWallManager.addCauseException(ctx.channel());
            ctx.channel().close();
        }
    }
}