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

package ua.nanit.limbo.server;

import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.configuration.LimboConfig;
import ua.nanit.limbo.connection.ClientChannelInitializer;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.PacketHandler;
import ua.nanit.limbo.connection.pipeline.compression.CompressionHandler;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.protocol.PacketSnapshots;
import ua.nanit.limbo.world.DimensionRegistry;

import java.io.IOException;

public final class LimboServer {

    private final LimboIntegration<ClientConnection> integration;
    private final ClientChannelInitializer clientChannelInitializer;
    private final LimboConfig config;
    private final PacketHandler packetHandler;
    //private final CommandHandler commandHandler;
    private final Connections connections;
    private final DimensionRegistry dimensionRegistry;
    //private final ScheduledFuture<?> keepAliveTask;

    public LimboServer(LimboIntegration<ClientConnection> integration) throws IOException {
        NanoLimbo.INTEGRATION = integration;
        NanoLimbo.LIMBO = this;

        Log.info("Starting virtual server...");
        this.integration = integration;
        this.config = new LimboConfig();

        //ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

        //this.commandHandler = integration.createCommandHandler();
        this.dimensionRegistry = new DimensionRegistry(this);
        this.dimensionRegistry.load("minecraft:" + config.getDimensionType().toLowerCase());
        this.connections = new Connections();

        PacketSnapshots.init();
        this.packetHandler = new PacketHandler(this);

        //keepAliveTask = null;//serverChannel.eventLoop().parent().scheduleAtFixedRate(this::broadcastKeepAlive, 0L, 5L, TimeUnit.SECONDS);

        //Runtime.getRuntime().addShutdownHook(new Thread(this::onDisable, "NanoLimbo shutdown thread"));

        this.clientChannelInitializer = new ClientChannelInitializer(this);
        //UiiaiuiiiaiCat.init();
        Log.info("Server started.");
    }

    public LimboIntegration<ClientConnection> getIntegration() {
        return integration;
    }

    public LimboConfig getConfig() {
        return config;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    /*public CommandHandler getCommandHandler() {
        return commandHandler;
    }*/

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

        this.connections.disconnectAll();
        CompressionHandler.releaseAll();
        PacketSnapshots.releaseAll();
        //if (keepAliveTask != null) keepAliveTask.cancel(true);

        Log.info("Server stopped.");
    }
}
