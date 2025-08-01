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

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTitle;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;
import ua.nanit.limbo.server.data.Title;

public class PacketTitleLegacy extends OutRetrooperPacket<WrapperPlayServerTitle> {

    public PacketTitleLegacy() {
        super(WrapperPlayServerTitle.class);
    }

    public PacketTitleLegacy setAction(WrapperPlayServerTitle.TitleAction action) {
        this.wrapper().setAction(action);
        return this;
    }

    public PacketTitleLegacy setTitle(Title title) {
        this.wrapper().setTitle(Component.text(title.getTitle()));
        this.wrapper().setSubtitle(Component.text(title.getSubtitle()));
        this.wrapper().setFadeInTicks(title.getFadeIn());
        this.wrapper().setStayTicks(title.getStay());
        this.wrapper().setFadeOutTicks(title.getFadeOut());
        return this;
    }
}
