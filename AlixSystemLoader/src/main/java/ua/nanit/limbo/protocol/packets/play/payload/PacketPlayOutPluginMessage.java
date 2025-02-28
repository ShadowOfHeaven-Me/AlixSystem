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

package ua.nanit.limbo.protocol.packets.play.payload;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.nio.charset.StandardCharsets;

public final class PacketPlayOutPluginMessage extends OutRetrooperPacket<WrapperPlayServerPluginMessage> {

    public PacketPlayOutPluginMessage() {
        super(WrapperPlayServerPluginMessage.class);
    }

    public PacketPlayOutPluginMessage(WrapperPlayServerPluginMessage wrapper) {
        super(wrapper);
    }

    public void setChannel(String channel) {
        this.wrapper().setChannelName(channel);
    }

    public String getChannel() {
        return this.wrapper().getChannelName();
    }

    public void setMessage(String message) {
        this.wrapper().setData(message.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getData() {
        return this.wrapper().getData();
    }
}