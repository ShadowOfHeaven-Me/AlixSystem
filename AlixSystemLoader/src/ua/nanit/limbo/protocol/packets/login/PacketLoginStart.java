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

package ua.nanit.limbo.protocol.packets.login;

import alix.libs.com.github.retrooper.packetevents.util.crypto.SignatureData;
import alix.libs.com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.InRetrooperPacket;
import ua.nanit.limbo.server.LimboServer;

import java.util.UUID;

public final class PacketLoginStart extends InRetrooperPacket<WrapperLoginClientLoginStart> {

    public PacketLoginStart() {
        super(WrapperLoginClientLoginStart.class);
    }

    public String getUsername() {
        return this.wrapper().getUsername();
    }

    public SignatureData getSignatureData() {
        return this.wrapper().getSignatureData().orElse(null);
    }

    public UUID getUUID() {
        return this.wrapper().getPlayerUUID().orElse(null);
    }

    @Override
    public void handle(ClientConnection conn, LimboServer server) {
        server.getPacketHandler().handle(conn, this);
    }
}
