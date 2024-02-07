package shadow.systems.login.firewall;

import alix.common.messages.Messages;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import shadow.Main;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;

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
    public final void channelActive(ChannelHandlerContext ctx) throws Exception {
        //Main.logError("ACTIVE: " + ctx);
        //ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("'mimimimimxxxw'".getBytes()));

        //ctx.close();
        super.channelActive(ctx);
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
        private final Object kickPacket = OutDisconnectKickPacketConstructor.constructAtLoginPhase(Messages.get("anti-bot-firewalled"));

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //Main.logError("REAAADD " + msg.getClass().getSimpleName());
            if (msg.getClass() == ReflectionUtils.loginInStartPacketClass)
                ctx.channel().writeAndFlush(kickPacket).addListener(ChannelFutureListener.CLOSE);
            else super.channelRead(ctx, msg);
        }
    }
}