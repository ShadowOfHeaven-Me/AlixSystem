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

package ua.nanit.limbo.connection.pipeline;

import alix.common.utils.netty.FastNettyUtils;
import io.netty.buffer.ByteBuf;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.State.PacketFactory;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;

public final class PacketDecoder {//extends MessageToMessageDecoder<ByteBuf> {

    /*private State.PacketRegistry mappings;
    private Version version;

    public PacketDecoder() {
        updateVersion(Version.getMin());
        updateState(State.HANDSHAKING);
    }*/

    //private static final ThreadLocal

    public static Packet decode(ByteBuf buf, State.PacketRegistry mappings, Version version, ClientConnection connection) {
        if (mappings == null) return null;

        //ByteMessage msg = new ByteMessage(buf);
        int packetId = FastNettyUtils.readVarInt(buf); //msg.readVarInt();
        //Log.error("PACKET ID= " + packetId);

        //Log.info("PACKET ID=" + packetId + " 0x" + Integer.toHexString(packetId).toUpperCase());

        /*if (packetId == 0x14) {
            Log.warning("PACKET=" + mappings.getPacket(packetId));
        }*/

        PacketFactory factory = mappings.getFactory(packetId);
        if (factory == null || connection != null && factory.isPacketSkippable(connection))
            return null;

        //Log.error("PACKET ID: " + packetId + " PACKET: " + packet);

        Packet packet = factory.newPacket();
        ByteMessage msg = new ByteMessage(buf);
        //if (packet != null) {
        if (Log.isDebug())
            Log.debug("Received packet %s[0x%s] (%d bytes)", packet.toString(), Integer.toHexString(packetId), msg.readableBytes());
        try {
            packet.decode(msg, version);
        } catch (Exception e) {
            connection.closeInvalidPacket();
            if (NanoLimbo.suppress(e)) return null;

            if (Log.isDebug()) {
                Log.warning("Cannot decode %s, per %s packet 0x%s", packet.getClass().getSimpleName(), e, Integer.toHexString(packetId));
            } else {
                Log.warning("Cannot decode %s packet 0x%s: %s", packet.getClass().getSimpleName(), Integer.toHexString(packetId), e.getMessage());
            }
            return null;
        }

        return packet;
        /*} else {
            //Log.debug("Undefined incoming packet: 0x" + Integer.toHexString(packetId));
        }*/
        //return null;
    }

    /*@Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (!ctx.channel().isActive() || mappings == null) return;

        ByteMessage msg = new ByteMessage(buf);
        int packetId = msg.readVarInt();
        Packet packet = mappings.getPacket(packetId);

        if (packet != null) {
            Log.debug("Received packet %s[0x%s] (%d bytes)", packet.toString(), Integer.toHexString(packetId), msg.readableBytes());
            try {
                packet.decode(msg, version);
            } catch (Exception e) {
                if (Log.isDebug()) {
                    Log.warning("Cannot decode packet 0x%s", e, Integer.toHexString(packetId));
                } else {
                    Log.warning("Cannot decode packet 0x%s: %s", Integer.toHexString(packetId), e.getMessage());
                }
            }

            ctx.fireChannelRead(packet);
        } else {
            Log.debug("Undefined incoming packet: 0x" + Integer.toHexString(packetId));
        }
    }

    public void updateVersion(Version version) {
        this.version = version;
    }

    public void updateState(State state) {
        this.mappings = state.serverBound.getRegistry(version);
    }*/
}
