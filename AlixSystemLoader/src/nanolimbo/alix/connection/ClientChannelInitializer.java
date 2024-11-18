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

package nanolimbo.alix.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import nanolimbo.alix.connection.pipeline.PacketDuplexHandler;
import nanolimbo.alix.connection.pipeline.VarIntFrameDecoder;
import nanolimbo.alix.server.LimboServer;
import nanolimbo.alix.server.Log;

public final class ClientChannelInitializer {

    private static final String
            duplexHandlerName = "--duplex_handler",
            frameDecoderName = "--frame_decoder";
    private final LimboServer server;

    public ClientChannelInitializer(LimboServer server) {
        this.server = server;
    }

    public void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        PacketDuplexHandler duplexHandler = new PacketDuplexHandler(channel, this.server);
        VarIntFrameDecoder frameDecoder = new VarIntFrameDecoder();
        //PacketDecoder decoder = new PacketDecoder();
        //PacketEncoder encoder = new PacketEncoder();

        ClientConnection connection = new ClientConnection(channel, server, duplexHandler, frameDecoder);
        duplexHandler.setClientConnection(connection);

        pipeline.addFirst(duplexHandlerName, duplexHandler);
        pipeline.addFirst(frameDecoderName, frameDecoder);

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

        //NoTimeoutHandler timeOutHandler = (NoTimeoutHandler) pipeline.context("timeout").handler();

        pipeline.replace("timeout","timeout", new ReadTimeoutHandler(30));
        pipeline.remove(duplexHandlerName);
        pipeline.remove(frameDecoderName);

        Log.error("UNINJECTED HANDLERS: " + pipeline.names());
        connection.resendCollected();
        //send packets - login start and handshake
        //config.setAutoRead(true);
    }
}
