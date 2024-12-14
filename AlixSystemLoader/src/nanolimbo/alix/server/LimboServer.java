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

package nanolimbo.alix.server;

import io.netty.channel.ChannelHandlerContext;
import nanolimbo.alix.commands.CommandHandler;
import nanolimbo.alix.configuration.LimboConfig;
import nanolimbo.alix.connection.ClientChannelInitializer;
import nanolimbo.alix.connection.PacketHandler;
import nanolimbo.alix.connection.pipeline.compression.CompressionHandler;
import nanolimbo.alix.integration.LimboIntegration;
import nanolimbo.alix.protocol.PacketSnapshots;
import nanolimbo.alix.world.DimensionRegistry;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

public final class LimboServer {

    private final LimboIntegration integration;
    private final ClientChannelInitializer clientChannelInitializer;
    private final LimboConfig config;
    private final PacketHandler packetHandler;
    private final CommandHandler commandHandler;
    private final Connections connections;
    private final DimensionRegistry dimensionRegistry;
    private final ScheduledFuture<?> keepAliveTask;

    public LimboServer(ChannelHandlerContext silentServerContext, LimboIntegration integration) throws IOException {
        this.integration = integration;
        config = new LimboConfig();

        Log.info("Starting server...");

        //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

        packetHandler = new PacketHandler(this);
        this.commandHandler = integration.createCommandHandler();
        dimensionRegistry = new DimensionRegistry(this);
        dimensionRegistry.load(config.getDimensionType());
        connections = new Connections();

        PacketSnapshots.initPackets(this);

        keepAliveTask = null;//serverChannel.eventLoop().parent().scheduleAtFixedRate(this::broadcastKeepAlive, 0L, 5L, TimeUnit.SECONDS);

        //Runtime.getRuntime().addShutdownHook(new Thread(this::onDisable, "NanoLimbo shutdown thread"));

        Log.info("Server started.");

        this.clientChannelInitializer = new ClientChannelInitializer(this, silentServerContext);
    }

    public LimboIntegration getIntegration() {
        return integration;
    }

    public LimboConfig getConfig() {
        return config;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public Connections getConnections() {
        return connections;
    }

    public DimensionRegistry getDimensionRegistry() {
        return dimensionRegistry;
    }

    public ClientChannelInitializer getClientChannelInitializer() {
        return clientChannelInitializer;
    }

    /*private void broadcastKeepAlive() {
        connections.getAllConnections().forEach(ClientConnection::sendKeepAlive);
    }*/

    public void onDisable() {
        Log.info("Stopping server...");

        CompressionHandler.releaseAll();
        PacketSnapshots.releaseAll();
        if (keepAliveTask != null) keepAliveTask.cancel(true);

        Log.info("Server stopped.");
    }
}
