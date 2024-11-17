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

import io.netty.channel.ChannelId;
import nanolimbo.alix.connection.ClientConnection;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Connections {

    private final Map<ChannelId, ClientConnection> connections;

    public Connections() {
        this.connections = new ConcurrentHashMap<>();
    }

    public Collection<ClientConnection> getAllConnections() {
        return connections.values();
    }

    public int getCount() {
        return connections.size();
    }

    public void addConnection(ClientConnection connection) {
        connections.put(connection.getChannel().id(), connection);

        connection.getChannel().closeFuture().addListener(future -> {
            this.removeConnection(connection);
        });
        Log.info("Player %s connected (%s) [%s]", connection.getUsername(),
                connection.getAddress(), connection.getClientVersion());
    }

    public void removeConnection(ClientConnection connection) {
        connections.remove(connection.getChannel().id());
        Log.info("Player %s disconnected", connection.getUsername());
    }
}
