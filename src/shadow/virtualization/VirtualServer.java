/*
package shadow.virtualization;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import shadow.Main;
import shadow.utils.objects.packet.types.verified.VerifiedPacketProcessor;
import shadow.virtualization.handler.PacketHandler;

//https://wiki.vg/Protocol_FAQ
public final class VirtualServer {

    public static void init(Channel channel) {
        channel.pipeline().addLast("sex", new ServerBootstrap().childHandler(new ChannelInit()));
        Main.logError("pipeline " + channel.pipeline().names());
        Bukkit.getServer().getIp();

    }

    private static final class ChannelInit extends ChannelInitializer<Channel> {
        @Override
        protected void initChannel(Channel channel) throws Exception {

        }
    }
}*/
