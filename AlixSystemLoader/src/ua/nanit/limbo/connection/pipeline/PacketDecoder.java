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
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.HandleMask;
import ua.nanit.limbo.protocol.Packet;
import ua.nanit.limbo.protocol.registry.State;
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

    static Packet decode(ByteBuf buf, State.PacketRegistry mappings, Version version) {
        if (mappings == null) return null;

        //ByteMessage msg = new ByteMessage(buf);
        int packetId = FastNettyUtils.readVarInt(buf); //msg.readVarInt();
        Packet packet = mappings.getPacket(packetId);
        if (packet == null) return null;

        /*long t = System.nanoTime();
        HandleMask.isSkippable(packet.getClass());
        long t2 = System.nanoTime();
        Log.error("TIME: " + (t2 - t) / Math.pow(10, 6) + "ms");*/

        //the Packet#handle method is unused - do not decode or call handle() on the packet instance
        //average time on my pc for this call: 0.002ms
        if (HandleMask.isSkippable(packet.getClass())) return null;
        //Log.error("PACKET ID: " + packetId + " PACKET: " + packet);

        ByteMessage msg = new ByteMessage(buf);
        //if (packet != null) {
        Log.debug("Received packet %s[0x%s] (%d bytes)", packet.toString(), Integer.toHexString(packetId), msg.readableBytes());
        try {
            packet.decode(msg, version);
        } catch (Exception e) {
            if (Log.isDebug()) {
                Log.warning("Cannot decode packet 0x%s", e, Integer.toHexString(packetId));
            } else {
                Log.warning("Cannot decode packet 0x%s: %s", Integer.toHexString(packetId), e.getMessage());
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