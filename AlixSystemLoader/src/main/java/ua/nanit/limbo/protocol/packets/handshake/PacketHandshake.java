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

package ua.nanit.limbo.protocol.packets.handshake;

import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketIn;
import ua.nanit.limbo.protocol.registry.State;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.LimboServer;

public final class PacketHandshake implements PacketIn {

    private Version version;
    private String host;
    private int port;
    private int nextState;

    public Version getVersion() {
        return version;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getNextStateValue() {
        return this.nextState;
    }

    public State getNextState() {
        return State.getHandshakeStateId(nextState);
    }

    @Override
    public void decode(ByteMessage msg, Version version) {
        /*try {
            this.version = Version.of(msg.readVarInt());
        } catch (IllegalArgumentException e) {
            Log.error("Zjebany handshake: " + msg);
            this.version = Version.UNDEFINED;
        }*/

        this.version = Version.of(msg.readVarInt());
        this.host = msg.readString();
        this.port = msg.readUnsignedShort();
        this.nextState = msg.readVarInt();
        //if (this.nextState == null) Log.error("Zjebany state");

        //Log.error("Wszysko: " + version + " host: " + host + " port: " + port + " nextState: " + nextState);
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeVarInt(this.version.getProtocolNumber());
        msg.writeString(this.host);
        msg.writeShort(this.port);
        msg.writeVarInt(this.nextState);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }
}
