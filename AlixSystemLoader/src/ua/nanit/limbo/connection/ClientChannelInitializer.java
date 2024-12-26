/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.handlers.DummyHandler;
import ua.nanit.limbo.server.LimboServer;

public final class ClientChannelInitializer {

    private static final String
            duplexHandlerName = "--duplex_handler",
            frameDecoderName = "--frame_decoder";
    private final LimboServer server;
    //private final ChannelInitImpl channelInitImpl;

    public ClientChannelInitializer(LimboServer server, ChannelHandlerContext silentServerContext) {
        this.server = server;
        //this.channelInitImpl = new ChannelInitImpl(silentServerContext);
    }

    public void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        PacketDuplexHandler duplexHandler = new PacketDuplexHandler(channel, this.server);
        VarIntFrameDecoder frameDecoder = new VarIntFrameDecoder();
        //PacketDecoder decoder = new PacketDecoder();
        //PacketEncoder encoder = new PacketEncoder();

        ClientConnection connection = this.server.getIntegration().newConnection(channel, server, duplexHandler, frameDecoder);
        duplexHandler.setClientConnection(connection);

        pipeline.addFirst(duplexHandlerName, duplexHandler);
        pipeline.addFirst(frameDecoderName, frameDecoder);

        //this.channelInitImpl.initialChannelRegister(channel);

        //pipeline.addLast("timeout", new ReadTimeoutHandler(server.getConfig().getReadTimeout(), TimeUnit.MILLISECONDS));
        //pipeline.addLast("frame_decoder", new VarIntFrameDecoder());
        //pipeline.addLast("frame_encoder", new VarIntLengthEncoder());

/*        if (server.getConfig().isUseTrafficLimits()) {
            pipeline.addLast("traffic_limit", new ChannelTrafficHandler(
                    server.getConfig().getMaxPacketSize(),
                    server.getConfig().getInterval(),
                    server.getConfig().getMaxPacketRate()
            ));
        }*/

        //pipeline.addLast("decoder", decoder);
        //pipeline.addLast("encoder", encoder);
        //pipeline.addLast("handler", connection);
    }

    public void uninject(ClientConnection connection) {
        //set autoRead to false after login start
        Channel channel = connection.getChannel();
        //ChannelConfig config = channel.config();
        ChannelPipeline pipeline = channel.pipeline();

        //config.setAutoRead(false);

        //config.setAutoRead(false);

        //NoTimeoutHandler timeOutHandler = (NoTimeoutHandler) pipeline.context("timeout").handler();

        if (pipeline.context("--timeout") != null) pipeline.replace("--timeout", "timeout", new ReadTimeoutHandler(30));
        //not sure why, but FlushConsolidationHandler causes some packets to not be sent at all by the main server, after I do this limbo voodoo magic
        boolean replacedFCH = pipeline.context("FlushConsolidationHandler#0") != null;
        if (replacedFCH)
            pipeline.replace("FlushConsolidationHandler#0", "--FlushConsolidationHandler#0", DummyHandler.HANDLER);

        pipeline.remove(duplexHandlerName);
        pipeline.remove(frameDecoderName);

        //Log.error("IN EVENT LOOP " + connection.getChannel().eventLoop().inEventLoop());

        pipeline.fireChannelRegistered();
        pipeline.fireChannelActive();

        connection.resendCollected();
        //after replacing it, it returns back to normal
        //still, unsure why
        if (replacedFCH)
            pipeline.replace("--FlushConsolidationHandler#0", "FlushConsolidationHandler#0", new FlushConsolidationHandler());

        //config.setAutoRead(true);
        //pipeline.firstContext()

        //Log.error("UNINJECTED HANDLERS: " + pipeline.names());

        //Gotta listen to the promise:
        //https://github.com/netty/netty/blob/4.1/transport/src/main/java/io/netty/channel/AbstractChannel.java#L802
        /*this.channelInitImpl.unregister(channel).addListener(f -> {
            if (!f.isSuccess()) {
                channel.unsafe().closeForcibly();
                return;
            }
            //this.channelInitImpl.invokeChannelReadInCtx(channel);
            Log.error("UNREGISTERED: " + pipeline.names());
            this.channelInitImpl.serverChannel().eventLoop().execute(() -> {

                Log.error("AFTER INVOKING CHANNEL READ: " + pipeline.names());
                this.server.getIntegration().invokeSilentServerChannelRead(channel);
                //https://github.com/netty/netty/blob/4.1/transport/src/main/java/io/netty/channel/AbstractChannel.java#L521
                //We've gotta re-fire channelActive manually
                channel.pipeline().fireChannelActive();
                Log.error("AFTER INVOKING CHANNEL READ: " + pipeline.names());

                channel.eventLoop().execute(() -> {
                    Log.error("eventLoop.execute " + pipeline.names());

                    config.setAutoRead(false);
                    connection.resendCollected();
                    config.setAutoRead(true);
                    Log.error("eventLoop.execute - END " + pipeline.names());
                });
            });
        });*/

        //send packets - login start and handshake
        //config.setAutoRead(true);
    }
}