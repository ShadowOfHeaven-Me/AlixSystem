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

import io.netty.channel.Channel;
import nanolimbo.alix.LimboIntegration;
import nanolimbo.alix.configuration.LimboConfig;
import nanolimbo.alix.connection.ClientChannelInitializer;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.connection.PacketHandler;
import nanolimbo.alix.connection.PacketSnapshots;
import nanolimbo.alix.world.DimensionRegistry;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class LimboServer {

    private final LimboIntegration integration;
    private final ClientChannelInitializer clientChannelInitializer;
    private final LimboConfig config;
    private final PacketHandler packetHandler;
    private final Connections connections;
    private final DimensionRegistry dimensionRegistry;
    private final ScheduledFuture<?> keepAliveTask;

    public LimboServer(Channel serverChannel, LimboIntegration integration) throws IOException {
        this.integration = integration;
        config = new LimboConfig();

        Log.info("Starting server...");

        packetHandler = new PacketHandler(this);
        dimensionRegistry = new DimensionRegistry(this);
        dimensionRegistry.load(config.getDimensionType());
        connections = new Connections();

        PacketSnapshots.initPackets(this);

        keepAliveTask = serverChannel.eventLoop().parent().scheduleAtFixedRate(this::broadcastKeepAlive, 0L, 5L, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "NanoLimbo shutdown thread"));

        Log.info("Server started.");

        this.clientChannelInitializer = new ClientChannelInitializer(this);
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

    public Connections getConnections() {
        return connections;
    }

    public DimensionRegistry getDimensionRegistry() {
        return dimensionRegistry;
    }

    public ClientChannelInitializer getClientChannelInitializer() {
        return clientChannelInitializer;
    }

    private void broadcastKeepAlive() {
        connections.getAllConnections().forEach(ClientConnection::sendKeepAlive);
    }

    private void stop() {
        Log.info("Stopping server...");

        if (keepAliveTask != null) {
            keepAliveTask.cancel(true);
        }

        Log.info("Server stopped, Goodbye!");
    }
}
