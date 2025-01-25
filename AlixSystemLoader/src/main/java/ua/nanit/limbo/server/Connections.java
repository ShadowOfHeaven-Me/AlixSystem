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

import io.netty.channel.ChannelId;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.UnsafeCloseFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class Connections {

    private final Map<ChannelId, ClientConnection> connections;
    private final AtomicInteger connectionsCount;

    public Connections() {
        this.connectionsCount = new AtomicInteger();
        this.connections = new ConcurrentHashMap<>();
    }

    /*public Collection<ClientConnection> getAllConnections() {
        return connections.values();
    }*/

    public int getCount() {
        return this.connectionsCount.get(); //connections.size();
    }

    public void disconnectAll() {
        this.connections.forEach((id, conn) -> UnsafeCloseFuture.unsafeClose(conn.getChannel()));
        this.connections.clear();
        //connectionsCount does not need to be set to 0
    }

    public void addConnection(ClientConnection connection) {
        this.connections.put(connection.getChannel().id(), connection);
        this.connectionsCount.incrementAndGet();

        connection.getChannel().closeFuture().addListener(future -> {
            this.removeConnection(connection);
        });
        //Log.info("Player %s connected (%s) [%s]", connection.getUsername(), connection.getAddress(), connection.getClientVersion());
    }

    private void removeConnection(ClientConnection connection) {
        this.connections.remove(connection.getChannel().id());
        this.connectionsCount.decrementAndGet();
        //Log.info("Player %s disconnected", connection.getUsername());
    }
}
