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

package ua.nanit.limbo.protocol.packets.play;

import alix.common.packets.message.MessageWrapper;
import alix.common.utils.formatter.AlixFormatter;
import alix.common.utils.netty.WrapperUtils;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.registry.Version;

public final class PacketPlayOutMessage implements PacketOut {

    private String message;

    public PacketPlayOutMessage() {
    }

    public static PacketSnapshot snapshot(String message) {
        return withMessage(message).toSnapshot();
    }

    public static PacketPlayOutMessage withMessage(String message) {
        return new PacketPlayOutMessage().setMessage(AlixFormatter.translateColors(message));
    }

    public PacketPlayOutMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public PacketWrapper<?> packetWrapper(Version version) {
        return MessageWrapper.createWrapper(Component.empty(), false, version.getRetrooperVersion());
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        var retrooperVersion = version.getRetrooperVersion();
        var wrapper = MessageWrapper.createWrapper(this.message, false, retrooperVersion);
        WrapperUtils.writeNoID(wrapper, msg.getBuf(), retrooperVersion);
    }
}
