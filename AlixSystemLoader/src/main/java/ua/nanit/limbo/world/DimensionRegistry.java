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


import alix.common.utils.other.throwable.AlixException;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.data.NamespacedKey;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;

public final class DimensionRegistry {

    private final CompoundBinaryTag codec_1_16;
    private final CompoundBinaryTag codec_1_16_2;
    private final CompoundBinaryTag codec_1_17;
    private final CompoundBinaryTag codec_1_18_2;
    private final CompoundBinaryTag codec_1_19;
    private final CompoundBinaryTag codec_1_19_1;
    private final CompoundBinaryTag codec_1_19_4;
    private final CompoundBinaryTag codec_1_20;
    private final CompoundBinaryTag codec_1_20_5;
    private final CompoundBinaryTag codec_1_21;
    private final CompoundBinaryTag codec_1_21_2;
    private final CompoundBinaryTag codec_1_21_4;
    private final CompoundBinaryTag codec_1_21_5;
    private final CompoundBinaryTag codec_1_21_6;
    private final CompoundBinaryTag codec_1_21_7;
    private final CompoundBinaryTag codec_1_21_9;
    private final CompoundBinaryTag codec_1_21_11;
    private final CompoundBinaryTag codec_26_1;
    private final CompoundBinaryTag codec_26_2;
    //private final CompoundBinaryTag oldCodec;
    private final CompoundBinaryTag tags_1_20_5;
    private final CompoundBinaryTag tags_1_21;
    private final CompoundBinaryTag tags_1_21_2;
    private final CompoundBinaryTag tags_1_21_4;
    private final CompoundBinaryTag tags_1_21_5;
    private final CompoundBinaryTag tags_1_21_6;
    private final CompoundBinaryTag tags_1_21_7;
    private final CompoundBinaryTag tags_1_21_9;
    private final CompoundBinaryTag tags_1_21_11;
    private final CompoundBinaryTag tags_26_1;
    private final CompoundBinaryTag tags_26_2;

    public DimensionRegistry() throws IOException {
        codec_1_16 = readNbtFile("codec_1_16");
        codec_1_16_2 = readNbtFile("codec_1_16_2");
        codec_1_17 = readNbtFile("codec_1_17");
        codec_1_18_2 = readNbtFile("codec_1_18_2");
        codec_1_19 = readNbtFile("codec_1_19");
        codec_1_19_1 = readNbtFile("codec_1_19_1");
        codec_1_19_4 = readNbtFile("codec_1_19_4");
        codec_1_20 = readNbtFile("codec_1_20");
        codec_1_20_5 = readNbtFile("codec_1_20_5");
        codec_1_21 = readNbtFile("codec_1_21");
        codec_1_21_2 = readNbtFile("codec_1_21_2");
        codec_1_21_4 = readNbtFile("codec_1_21_4");
        codec_1_21_5 = readNbtFile("codec_1_21_5");
        codec_1_21_6 = readNbtFile("codec_1_21_6");
        codec_1_21_7 = readNbtFile("codec_1_21_7");
        codec_1_21_9 = readNbtFile("codec_1_21_9");
        codec_1_21_11 = readNbtFile("codec_1_21_11");
        codec_26_1 = readNbtFile("codec_26_1");
        codec_26_2 = readNbtFile("codec_26_2");

        tags_1_20_5 = readNbtFile("tags_1_20_5");
        tags_1_21 = readNbtFile("tags_1_21");
        tags_1_21_2 = readNbtFile("tags_1_21_2");
        tags_1_21_4 = readNbtFile("tags_1_21_4");
        tags_1_21_5 = readNbtFile("tags_1_21_5");
        tags_1_21_6 = readNbtFile("tags_1_21_6");
        tags_1_21_7 = readNbtFile("tags_1_21_7");
        tags_1_21_9 = readNbtFile("tags_1_21_9");
        tags_1_21_11 = readNbtFile("tags_1_21_11");
        tags_26_1 = readNbtFile("tags_26_1");
        tags_26_2 = readNbtFile("tags_26_2");
    }

    //https://github.com/Nan1t/NanoLimbo/blob/149434d7dc588cfa1b499205778c9aef12ad6341/src/main/java/ua/nanit/limbo/world/DimensionRegistry.java
    public CompoundBinaryTag getCodec(Version version) {
        if (version.moreOrEqual(Version.V26_2))
            return this.codec_26_2;
        if (version.moreOrEqual(Version.V26_1))
            return this.codec_26_1;
        if (version.moreOrEqual(Version.V1_21_11))
            return this.codec_1_21_11;
        if (version.equals(Version.V1_21_9))
            return this.codec_1_21_9;
        if (version.equals(Version.V1_21_7))
            return this.codec_1_21_7;
        if (version.equals(Version.V1_21_6))
            return this.codec_1_21_6;
        if (version.equals(Version.V1_21_5))
            return this.codec_1_21_5;
        if (version.equals(Version.V1_21_4))
            return this.codec_1_21_4;
        if (version.equals(Version.V1_21_2))
            return this.codec_1_21_2;
        if (version.equals(Version.V1_21))
            return this.codec_1_21;
        if (version.equals(Version.V1_20_5))
            return this.codec_1_20_5;
        if (version.moreOrEqual(Version.V1_20))
            return this.codec_1_20;
        if (version.equals(Version.V1_19_4))
            return this.codec_1_19_4;
        if (version.moreOrEqual(Version.V1_19_1))
            return this.codec_1_19_1;
        if (version.equals(Version.V1_19))
            return this.codec_1_19;
        if (version.equals(Version.V1_18_2))
            return this.codec_1_18_2;
        if (version.moreOrEqual(Version.V1_17))
            return this.codec_1_17;
        if (version.moreOrEqual(Version.V1_16_2))
            return this.codec_1_16_2;
        return this.codec_1_16;
    }
    
    public CompoundBinaryTag getTags(Version version) {
        return switch (version) {
            case V26_2 -> this.tags_26_2;
            case V26_1 -> this.tags_26_1;
            case V1_21_11 -> this.tags_1_21_11;
            case V1_21_9 -> this.tags_1_21_9;
            case V1_21_7 -> this.tags_1_21_7;
            case V1_21_6 -> this.tags_1_21_6;
            case V1_21_5 -> this.tags_1_21_5;
            case V1_21_4 -> this.tags_1_21_4;
            case V1_21_2 -> this.tags_1_21_2;
            case V1_21 -> this.tags_1_21;
            case V1_20_5 -> this.tags_1_20_5;
            default -> throw new AlixException("Unexpected value: " + version);
        };
    }

    public Dimension findDimension(Version version, NamespacedKey dimensionKey) {
        CompoundBinaryTag codec = getCodec(version);
        BiFunction<Integer, CompoundBinaryTag, Dimension> findModernDimension = (index, dimensionTag) -> {
            String name = dimensionTag.getString("name");
            if (name.equals(dimensionKey.toString())) {
                CompoundBinaryTag elementTag = (CompoundBinaryTag) dimensionTag.get("element");

                int id = dimensionTag.getInt("id");
                if (elementTag != null) {
                    int height = elementTag.getInt("height");
                    return new Dimension(dimensionKey, id, height, codec, elementTag);
                }
            }

            return null;
        };

        Dimension modern = findDefaultDimension(codec, findModernDimension);
        if (modern != null) {
            return modern;
        }

        BiFunction<Integer, CompoundBinaryTag, Dimension> findLegacyDimension = (index, dimensionTag) -> {
            String name = dimensionTag.getString("name");
            if (name.equals(dimensionKey.toString())) {
                int height = dimensionTag.getInt("height");
                return new Dimension(dimensionKey, index, height, codec, dimensionTag);
            }

            return null;
        };
        return findDefaultDimension(codec, findLegacyDimension);
    }

    private static <T> T findDefaultDimension(CompoundBinaryTag codec, BiFunction<Integer, CompoundBinaryTag, T> function) {
        ListBinaryTag dimensions;
        BinaryTag binaryDimensionType = codec.get("minecraft:dimension_type");
        if (binaryDimensionType instanceof CompoundBinaryTag tag) {
            dimensions = tag.getList("value");
        } else {
            dimensions = codec.getList("dimension");
        }

        for (int i = 0; i < dimensions.size(); i++) {
            BinaryTag dimension = dimensions.get(i);

            CompoundBinaryTag dimensionTag = (CompoundBinaryTag) dimension;

            T result = function.apply(i, dimensionTag);
            if (result != null) {
                return result;
            }
        }

        CompoundBinaryTag defaultDimension = (CompoundBinaryTag) dimensions.get(0);
        return function.apply(0, defaultDimension);
    }

    public CompoundBinaryTag getCodec_1_20() {
        return codec_1_20;
    }

    private CompoundBinaryTag readNbtFile(String fileName) throws IOException {
        return this.readNbtFile0("ua.nanit.limbo.world.dimension/" + fileName + ".nbt");
    }

    private CompoundBinaryTag readNbtFile0(String resPath) throws IOException {
        InputStream in = DimensionRegistry.class.getClassLoader().getResourceAsStream(resPath);

        if (in == null)
            throw new AlixException("Cannot find nbt file " + resPath);

        return BinaryTagIO.unlimitedReader().read(in, BinaryTagIO.Compression.GZIP);
    }
}
