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

import ua.nanit.limbo.protocol.ByteMessage;
import ua.nanit.limbo.protocol.NbtMessage;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.util.NbtMessageUtil;

import java.util.UUID;

public final class PacketPlayOutMessage implements PacketOut {

    private static final UUID sender = new UUID(53, 35);
    private PositionLegacy position;
    private NbtMessage message;

    public static PacketSnapshot snapshot(String message) {
        return withMessage(message).toSnapshot();
    }

    public static PacketPlayOutMessage withMessage(String message) {
        return new PacketPlayOutMessage().setMessage(NbtMessageUtil.fromLiteral(message)).setPosition(PositionLegacy.SYSTEM_MESSAGE);
    }

    public PacketPlayOutMessage setMessage(NbtMessage message) {
        this.message = message;
        return this;
    }

    public PacketPlayOutMessage setPosition(PositionLegacy position) {
        this.position = position;
        return this;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeNbtMessage(message, version);
        if (version.moreOrEqual(Version.V1_19_1)) {
            msg.writeBoolean(position.index == PositionLegacy.ACTION_BAR.index);
        } else if (version.moreOrEqual(Version.V1_19)) {
            msg.writeVarInt(position.index);
        } else if (version.moreOrEqual(Version.V1_8)) {
            msg.writeByte(position.index);
        }

        if (version.moreOrEqual(Version.V1_16) && version.less(Version.V1_19))
            msg.writeUuid(sender);
    }

    public enum PositionLegacy {

        CHAT(0),
        SYSTEM_MESSAGE(1),
        ACTION_BAR(2);

        private final int index;

        PositionLegacy(int index) {
            this.index = index;
        }

    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
