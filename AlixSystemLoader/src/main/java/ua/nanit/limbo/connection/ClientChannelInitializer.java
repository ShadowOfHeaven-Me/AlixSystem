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

import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.environment.ServerEnvironment;
import alix.common.utils.floodgate.GeyserUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.connection.pipeline.compression.ClientCompressSupply;
import ua.nanit.limbo.connection.pipeline.encryption.EncryptionSupplier;
import ua.nanit.limbo.integration.StateSupplier;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

import java.util.function.Consumer;

public final class ClientChannelInitializer {

    public static final String
            duplexHandlerName = "--duplex_handler",
            frameDecoderName = "--frame_decoder",
            proxyDecoderName = "--haproxy_decoder";
    private final LimboServer server;
    //private final ChannelInitImpl channelInitImpl;

    public ClientChannelInitializer(LimboServer server) {
        this.server = server;
        //this.channelInitImpl = new ChannelInitImpl(silentServerContext);
    }

    public void initAfterLimboLogin(ClientConnection connection, PersistentUserData data, Consumer<ClientConnection> authAction, GeyserUtil geyserUtil) {
        connection.getVerifyState().setData(data, authAction, geyserUtil);
    }

    public void initAfterLoginSuccess(Channel channel, Version clientVersion, String name, ClientCompressSupply compress, EncryptionSupplier cipherSupplier, Consumer<ClientConnection> authAction, GeyserUtil geyserUtil) {
        //Log.warning("initAfterLoginSuccess=" + name);
        ChannelPipeline pipeline = channel.pipeline();
        ClientConnection connection = this.server.getIntegration().newConnection(channel, server, true);

        connection.setVerifyState(StateSupplier.LOGIN);

        Runnable r = () -> {
            //Ensure thread locality by setting fields on the client's netty thread
            connection.updateVersion(clientVersion);
            connection.updateState(connection.hasConfigPhase() ? State.CONFIGURATION : State.PLAY);
            connection.getGameProfile().setUsername(name);

            PacketDuplexHandler duplexHandler = connection.getDuplexHandler();
            VarIntFrameDecoder frameDecoder = connection.getFrameDecoder();

            var cipher = cipherSupplier.getHandlerFor(connection);
            connection.setCipher(cipher);
            frameDecoder.stopResendCollection();

            connection.getVerifyState().setData(UserFileManager.get(name), authAction, geyserUtil);

            pipeline.addFirst(duplexHandlerName, duplexHandler);
            pipeline.addFirst(frameDecoderName, frameDecoder);

            this.server.getConnections().addConnection(connection);

            //frameDecoder.stopResendCollection();
            if (compress.shouldEnableCompress(connection)) {
                boolean success = duplexHandler.tryEnableCompression(false, true);
                if (!success)
                    Log.warning("COULD NOT ENABLE COMPRESSION FOR " + name + " ");
            }

            if (connection.hasConfigPhase()) connection.onLoginAcknowledgedReceived();
            else connection.spawnPlayer();
        };

        var eventLoop = channel.eventLoop();
        if (!eventLoop.inEventLoop()) eventLoop.execute(r);
        else r.run();
    }

    private boolean isFloodgatePresent() {
        return NanoLimbo.INTEGRATION.geyserUtil().isFloodgatePresent();
    }

    public void initChannel(Channel channel, boolean proxyProtocol, boolean passActiveEvents) {
        ChannelPipeline pipeline = channel.pipeline();

        ClientConnection connection = this.server.getIntegration().newConnection(channel, server, passActiveEvents);
        PacketDuplexHandler duplexHandler = connection.getDuplexHandler();
        VarIntFrameDecoder frameDecoder = connection.getFrameDecoder();

        pipeline.addFirst(duplexHandlerName, duplexHandler);
        pipeline.addFirst(frameDecoderName, frameDecoder);

        if (proxyProtocol)
            pipeline.addFirst(proxyDecoderName, new HAProxyMessageDecoder());

        LimboJoinProfiler.update(channel, ConnectionStage.CHANNEL_INIT, "pipeline=" + pipeline.names());
    }

    public void uninjectHandlersAndConnection(ClientConnection connection) {
        Channel channel = connection.getChannel();
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.context(duplexHandlerName) != null) {
            //make sure to always remove the duplex handler first,
            // so that the frame decoder can pass the cumulation to other handlers during handlerRemoved
            pipeline.remove(duplexHandlerName);
            pipeline.remove(frameDecoderName);
        }

        if (NanoLimbo.debugMode)
            Log.error("uninjectHandlersAndConnection, PIPELINE=" + pipeline.names());

        this.server.getConnections().removeConnection0(connection);
    }

    public void uninject(ClientConnection connection, Runnable resendAction) {
        //set autoRead to false after login start
        Channel channel = connection.getChannel();
        ChannelConfig config = channel.config();
        ChannelPipeline pipeline = channel.pipeline();

        config.setAutoRead(false);

        if (pipeline.context("--timeout") != null)
            pipeline.replace("--timeout", "timeout", new ReadTimeoutHandler(30));
        //not sure why, but FlushConsolidationHandler causes some packets to not be sent at all by the main server, after I do this limbo voodoo magic

        /*var fchName = "FlushConsolidationHandler#0";
        var prefixedFchName = "--FlushConsolidationHandler#0";

        boolean replacedFCH = pipeline.context(fchName) != null;
        if (replacedFCH)
            pipeline.replace(fchName, prefixedFchName, DummyHandler.HANDLER);*/

        this.uninjectHandlersAndConnection(connection);

        NanoLimbo.INTEGRATION.invokeChannelInit(connection);

        boolean proxyProtocol = this.server.getIntegration().isProxyProtocol();

        if (proxyProtocol) {
            var haDecoderPlatformName = ServerEnvironment.isVelocity() ? "HAProxyMessageDecoder#0" : "haproxy-decoder";

            //nuke velocity's handler, the haProxyMessage should already be decoded by our own handler
            pipeline.remove(haDecoderPlatformName);
            if (pipeline.context(proxyDecoderName) != null)//well I'll be damned if it isn't
                Log.warning("proxyDecoder present! Decoded message not null: " + (connection.getFrameDecoder().haProxyMessage != null));
        }

        pipeline.fireChannelRegistered();
        pipeline.fireChannelActive();

        if (this.server.getIntegration().isProxyProtocol()) {
            var msg = connection.getFrameDecoder().haProxyMessage;

            if (msg != null)
                pipeline.fireChannelRead(msg);
            else
                Log.warning("Missing HA-Proxy Message with proxy protocol ON!");
        }

        //resend the correct packets to the server
        resendAction.run();

        //inform everyone that this is all there is to read
        pipeline.fireChannelReadComplete();

        config.setAutoRead(true);

        //after replacing it, it returns back to normal
        //still, unsure why
        /*if (replacedFCH && pipeline.context(prefixedFchName) != null)
            pipeline.replace(prefixedFchName, fchName, new FlushConsolidationHandler());*/

        if (LimboJoinProfiler.PROFILE_JOINS)
            LimboJoinProfiler.update(channel, ConnectionStage.UNINJECT_FINISHED, "pipeline=" + channel.pipeline().names());

        if (NanoLimbo.debugMode)
            Log.warning("uninject pipeline=" + channel.pipeline().names());
    }
}