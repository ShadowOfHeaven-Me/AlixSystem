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

package ua.nanit.limbo.protocol.packets.login.disconnect;

import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

public class PacketLoginDisconnect extends OutRetrooperPacket<WrapperLoginServerDisconnect> {

    public PacketLoginDisconnect() {
        super(WrapperLoginServerDisconnect.class);
    }

    public PacketLoginDisconnect setReason(String reason) {
        this.wrapper().setReason(Component.text(reason));
        return this;
    }

    public static PacketSnapshot snapshot(String reason) {
        return new PacketLoginDisconnect().setReason(reason).toSnapshot();
    }
}
