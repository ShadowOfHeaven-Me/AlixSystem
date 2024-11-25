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

package nanolimbo.alix.connection.pipeline;

import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.Packet;
import nanolimbo.alix.protocol.registry.State;
import nanolimbo.alix.protocol.registry.Version;
import nanolimbo.alix.server.Log;

public final class PacketEncoder {

    static ByteMessage encode(Packet packet, State.PacketRegistry registry, Version version) {
        if (registry == null) return null;

        ByteMessage msg = ByteMessage.create();
        int packetId = registry.getPacketId(packet.getClass());

        /*if (packet instanceof PacketSnapshot) {
            packetId = registry.getPacketId(((PacketSnapshot) packet).getWrappedPacket().getClass());
        } else {
            packetId = registry.getPacketId(packet.getClass());
        }*/

        if (packetId < 0) {
            Log.warning("Undefined packet class: %s[0x%s] (%d bytes)", packet.getClass().getName(), Integer.toHexString(packetId), msg.readableBytes());
            return null;
        }

        msg.writeVarInt(packetId);

        try {
            packet.encode(msg, version);

            if (Log.isDebug()) {
                Log.debug("Sending %s[0x%s] packet (%d bytes)", packet.toString(), Integer.toHexString(packetId), msg.readableBytes());
            }
            return msg;
        } catch (Exception e) {
            Log.error("Cannot encode packet 0x%s (%s): %s", Integer.toHexString(packetId), packet.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
