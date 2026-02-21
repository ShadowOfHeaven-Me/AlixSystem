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

import alix.common.data.file.UserFileManager;
import alix.common.utils.floodgate.GeyserUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.captcha.CaptchaState;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.connection.pipeline.compression.ClientCompressSupply;
import ua.nanit.limbo.connection.pipeline.encryption.EncryptionSupplier;
import ua.nanit.limbo.handlers.DummyHandler;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

import java.util.function.Consumer;

public final class ClientChannelInitializer {

    public static final String
            duplexHandlerName = "--duplex_handler",
            frameDecoderName = "--frame_decoder";
    private final LimboServer server;
    //private final ChannelInitImpl channelInitImpl;

    public ClientChannelInitializer(LimboServer server) {
        this.server = server;
        //this.channelInitImpl = new ChannelInitImpl(silentServerContext);
    }

    public void initAfterLoginSuccess(Channel channel, Version clientVersion, String name, ClientCompressSupply compress, EncryptionSupplier cipherSupplier, Consumer<ClientConnection> authAction, GeyserUtil geyserUtil) {
        ChannelPipeline pipeline = channel.pipeline();
        ClientConnection connection = this.server.getIntegration().newConnection(channel, server, LoginState::new);

        Runnable r = () -> {
            //Ensure thread locality by setting fields on the client's netty thread
            connection.updateVersion(clientVersion);
            connection.updateState(connection.hasConfigPhase() ? State.CONFIGURATION : State.PLAY);
            connection.getGameProfile().setUsername(name);

            PacketDuplexHandler duplexHandler = connection.getDuplexHandler();
            VarIntFrameDecoder frameDecoder = connection.getFrameDecoder();;

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

            //todo: add HAProxyMessageDecoder support

            //AlixCommonMain.logError("sex: " + pipeline.names());

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

    public void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        ClientConnection connection = this.server.getIntegration().newConnection(channel, server, CaptchaState::new);
        PacketDuplexHandler duplexHandler = connection.getDuplexHandler();
        VarIntFrameDecoder frameDecoder = connection.getFrameDecoder();

        pipeline.addFirst(duplexHandlerName, duplexHandler);
        pipeline.addFirst(frameDecoderName, frameDecoder);

        /*if (this.isFloodgatePresent() && pipeline.context("GeyserVelocityInjector$3#0") != null) {
            pipeline.addAfter("GeyserVelocityInjector$3#0", duplexHandlerName, duplexHandler);
            pipeline.addAfter("GeyserVelocityInjector$3#0", frameDecoderName, frameDecoder);
        } else {
            pipeline.addFirst(duplexHandlerName, duplexHandler);
            pipeline.addFirst(frameDecoderName, frameDecoder);
        }*/
        //PacketDecoder decoder = new PacketDecoder();
        //PacketEncoder encoder = new PacketEncoder();
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
        //ChannelConfig config = channel.config();
        ChannelPipeline pipeline = channel.pipeline();

        //config.setAutoRead(false);

        //config.setAutoRead(false);

        //NoTimeoutHandler timeOutHandler = (NoTimeoutHandler) pipeline.context("timeout").handler();

        if (pipeline.context("--timeout") != null)
            pipeline.replace("--timeout", "timeout", new ReadTimeoutHandler(30));
        //not sure why, but FlushConsolidationHandler causes some packets to not be sent at all by the main server, after I do this limbo voodoo magic

        var fchName = "FlushConsolidationHandler#0";
        var prefixedFchName = "--FlushConsolidationHandler#0";

        boolean replacedFCH = pipeline.context(fchName) != null;
        if (replacedFCH)
            pipeline.replace(fchName, prefixedFchName, DummyHandler.HANDLER);

        this.uninjectHandlersAndConnection(connection);

        //Log.error("IN EVENT LOOP " + connection.getChannel().eventLoop().inEventLoop());

        pipeline.fireChannelRegistered();
        pipeline.fireChannelActive();

        //resend the correct packets to the server
        resendAction.run();

        //after replacing it, it returns back to normal
        //still, unsure why
        if (replacedFCH && pipeline.context(prefixedFchName) != null)
            pipeline.replace(prefixedFchName, fchName, new FlushConsolidationHandler());

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