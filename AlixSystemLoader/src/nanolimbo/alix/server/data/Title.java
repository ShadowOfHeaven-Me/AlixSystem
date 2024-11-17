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

package nanolimbo.alix.server.data;

import nanolimbo.alix.protocol.NbtMessage;

public class Title {

    private NbtMessage title;
    private NbtMessage subtitle;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    public NbtMessage getTitle() {
        return title;
    }

    public NbtMessage getSubtitle() {
        return subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public void setTitle(NbtMessage title) {
        this.title = title;
    }

    public void setSubtitle(NbtMessage subtitle) {
        this.subtitle = subtitle;
    }

    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public void setStay(int stay) {
        this.stay = stay;
    }

    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }
}
