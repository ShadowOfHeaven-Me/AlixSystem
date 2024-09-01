/*
package shadow.systems.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import shadow.Main;

public final class AlixInjector {

    private static final String name = "alix-processor-injector";

    static void injectIntoServerPipeline(ChannelPipeline pipeline) {
        if (pipeline.context(name) != null) pipeline.remove(name);

        Class<?> bootstrap = null;
        Class<?>[] a = ServerBootstrap.class.getDeclaredClasses();

        if (a.length == 1) bootstrap = a[0];
        else for (Class<?> c : a) if (c.getSimpleName().equals("ServerBootstrapAcceptor")) bootstrap = c;

        if (bootstrap == null) bootstrap = a[0];

        String serverName = pipeline.context((Class<? extends ChannelHandler>) bootstrap).name();
        pipeline.addBefore(serverName, name, new InjectorImpl());
    }

    private static final class InjectorImpl extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            Main.logError("HANDLERS: " + ctx.pipeline().names());
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
            Main.logError("REGISTERED: " + ctx.pipeline().names());
        }
    }
}*/
