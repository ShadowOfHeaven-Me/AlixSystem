package shadow.utils.objects.packet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import shadow.utils.objects.packet.types.verified.SelfDelegatingProcessor;

import java.util.List;

public final class PacketInterceptor extends ChannelDuplexHandler {

    //private static final ChannelInjector channelInjector = AlixHandler.createChannelInjectorImpl();
    private final Channel channel;
    private final ChannelHandlerContext afterAlixHandlerContext;
    private PacketProcessor processor = new SelfDelegatingProcessor(this);//delegate to self in order to deliver all packets, before we set another processor later on

    //read -> (decompression, buffers to packets, etc.) -> alix_handler -> packet_handler
    //write -> packet_handler -> alix_handler -> (compression, packets to buffers, etc.)

    /*public PacketInterceptor(Player player) {
        this(getChannel(player));
    }*/

    //This code finds a Channel Handler placed before the server's "packet_handler".
    //Alix's PacketInterceptor is injected before "packet_handler", and with PacketBlocker
    //or other PacketProcessors not all packets will be delivered to the player.
    //The invocation now looks like this:
    //write -> packet_handler -> alix_handler -> (some another handler after alix)
    //However, we want our own packet to be delivered, and we can bypass the checks of the PacketProcessor
    //by getting the ChannelHandlerContext that's right after the "packet_handler", since the alix_handler
    //hasn't been injected yet. This way, the 'silent' invocation looks like this:
    //write -> (some another handler after alix)
    public PacketInterceptor(Channel channel) {
        this.channel = channel;
        List<String> handlers = channel.pipeline().names();
        String name = handlers.get(handlers.indexOf("packet_handler") - 1);
        this.afterAlixHandlerContext = this.channel.pipeline().context(name);
    }

    //Inspired by: https://github.com/retrooper/packetevents/blob/2.0/api/src/main/java/com/github/retrooper/packetevents/protocol/player/User.java#L143

    public final void writeAndFlushSilently(Object msg) {
        this.afterAlixHandlerContext.writeAndFlush(msg);
    }

    public final void writeSilently(Object msg) {
        this.afterAlixHandlerContext.write(msg);
    }

    public final void flushSilently() {
        this.afterAlixHandlerContext.flush();
    }

    public final ChannelHandlerContext getSilentContext() {
        return this.afterAlixHandlerContext;
    }

/*    public final void inject(PacketProcessor processor) {
        this.setProcessor(processor);
        channel.pipeline().addBefore("packet_handler", packetHandlerName, this);
        //Bukkit.broadcastMessage("ZET " + channel.pipeline().names() + " " + afterAlixHandlerContext.name());
    }*/

    public final void setProcessor(PacketProcessor processor) {
        this.processor = processor;
    }

    /*public static Channel getChannel(Player player) {
        try {
            return channelInjector.getChannel(player);
        } catch (Exception e) {
            player.kickPlayer("§cCould not get the netty channel. Report this as an error immediately!");
            throw new RuntimeException(e);
        }
    }*/

    public final Channel getChannel() {
        return channel;
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.processor.channelRead(ctx, msg);
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        this.processor.write(ctx, msg, promise);
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.processor.exceptionCaught(ctx, cause);
    }

    final void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    final void write0(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    public static void init() {
    }
}