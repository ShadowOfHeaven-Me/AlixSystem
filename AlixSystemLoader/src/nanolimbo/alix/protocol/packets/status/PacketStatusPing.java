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

package nanolimbo.alix.protocol.packets.status;

import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.Packet;
import nanolimbo.alix.protocol.registry.Version;
import nanolimbo.alix.server.LimboServer;

public class PacketStatusPing implements Packet {

    private long randomId;

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeLong(randomId);
    }

    @Override
    public void decode(ByteMessage msg, Version version) {
        this.randomId = msg.readLong();
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
