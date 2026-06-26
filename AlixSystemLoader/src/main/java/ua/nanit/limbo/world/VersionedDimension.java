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

package ua.nanit.limbo.world;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.data.NamespacedKey;
import ua.nanit.limbo.util.map.VersionMap;

@RequiredArgsConstructor
public class VersionedDimension {

    @Getter
    private final NamespacedKey key;
    @Getter
    private final int legacyDimensionId;
    private final VersionMap<Dimension> perVersionDimensions;

    public int getId(@NonNull Version version) {
        return getDimensionByProtocol(version).id();
    }

    public int getHeight(@NonNull Version version) {
        return getDimensionByProtocol(version).height();
    }

    public int getChunkSections(@NonNull Version version) {
        return getHeight(version) / 16;
    }

    @NonNull
    public CompoundBinaryTag getCodec(@NonNull Version version) {
        return getDimensionByProtocol(version).codec();
    }

    @NonNull
    public CompoundBinaryTag getDefaultCodec(@NonNull Version version) {
        return getDimensionByProtocol(version).defaultCodec();
    }

    @NonNull
    private Dimension getDimensionByProtocol(@NonNull Version version) {
        Dimension dimension = this.perVersionDimensions.get(version);
        if (dimension == null) {
            throw new IllegalStateException("No dimension found for version " + version);
        }

        return dimension;
    }

}