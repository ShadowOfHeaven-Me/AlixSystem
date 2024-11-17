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

package nanolimbo.alix.protocol.packets.login;

import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.registry.Version;

import java.util.UUID;

public class PacketLoginSuccess implements PacketOut {

    private UUID uuid;
    private String username;

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        if (version.moreOrEqual(Version.V1_16)) {
            msg.writeUuid(uuid);
        } else if (version.moreOrEqual(Version.V1_7_6)) {
            msg.writeString(uuid.toString());
        } else {
            msg.writeString(uuid.toString().replace("-", ""));
        }
        msg.writeString(username);
        if (version.moreOrEqual(Version.V1_19)) {
            msg.writeVarInt(0);
        }
        if (version.moreOrEqual(Version.V1_20_5)) {
            msg.writeBoolean(true);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
