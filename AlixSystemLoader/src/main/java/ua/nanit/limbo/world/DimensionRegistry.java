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
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

import java.io.IOException;
import java.io.InputStream;

public final class DimensionRegistry {

    private final LimboServer server;

    private final Dimension defaultDimension_1_16;
    private final Dimension defaultDimension_1_16_2;
    private final Dimension defaultDimension_1_17;
    private final Dimension defaultDimension_1_18_2;
    private final Dimension dimension_1_20_5;
    private final Dimension dimension_1_21;
    private final Dimension dimension_1_21_2;
    private final Dimension dimension_1_21_4;
    private final Dimension dimension_1_21_5;
    private final Dimension dimension_1_21_6;

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
    //private final CompoundBinaryTag oldCodec;
    private final CompoundBinaryTag tags_1_20_5;
    private final CompoundBinaryTag tags_1_21;
    private final CompoundBinaryTag tags_1_21_2;
    private final CompoundBinaryTag tags_1_21_4;
    private final CompoundBinaryTag tags_1_21_5;
    private final CompoundBinaryTag tags_1_21_6;
    private final CompoundBinaryTag tags_1_21_7;

    public DimensionRegistry(LimboServer server, String def) throws IOException {
        this.server = server;

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

        tags_1_20_5 = readNbtFile("tags_1_20_5");
        tags_1_21 = readNbtFile("tags_1_21");
        tags_1_21_2 = readNbtFile("tags_1_21_2");
        tags_1_21_4 = readNbtFile("tags_1_21_4");
        tags_1_21_5 = readNbtFile("tags_1_21_5");
        tags_1_21_6 = readNbtFile("tags_1_21_6");
        tags_1_21_7 = readNbtFile("tags_1_21_7");

        defaultDimension_1_16 = getLegacyDimension(def);
        defaultDimension_1_16_2 = getModernDimension(def, codec_1_16_2);
        defaultDimension_1_17 = getModernDimension(def, codec_1_17);
        defaultDimension_1_18_2 = getModernDimension(def, codec_1_18_2);

        dimension_1_20_5 = getModernDimension(def, codec_1_20_5);
        dimension_1_21 = getModernDimension(def, codec_1_21);
        dimension_1_21_2 = getModernDimension(def, codec_1_21_2);
        dimension_1_21_4 = getModernDimension(def, codec_1_21_4);
        dimension_1_21_5 = getModernDimension(def, codec_1_21_5);
        dimension_1_21_6 = getModernDimension(def, codec_1_21_6);
    }

    public CompoundBinaryTag getCodec_1_16() {
        return codec_1_16;
    }

    public CompoundBinaryTag getCodec_1_18_2() {
        return codec_1_18_2;
    }

    public CompoundBinaryTag getCodec_1_19() {
        return codec_1_19;
    }

    public CompoundBinaryTag getCodec_1_19_1() {
        return codec_1_19_1;
    }

    public CompoundBinaryTag getCodec_1_19_4() {
        return codec_1_19_4;
    }

    public CompoundBinaryTag getCodec_1_20() {
        return codec_1_20;
    }

    public CompoundBinaryTag getCodec_1_20_5() {
        return codec_1_20_5;
    }

    public CompoundBinaryTag getCodec_1_21() {
        return codec_1_21;
    }

    public CompoundBinaryTag getCodec_1_21_2() {
        return codec_1_21_2;
    }

    public CompoundBinaryTag getCodec_1_21_4() {
        return codec_1_21_4;
    }

    /*public CompoundBinaryTag getOldCodec() {
        return oldCodec;
    }*/

    public Dimension getDefaultDimension_1_16() {
        return defaultDimension_1_16;
    }

    public Dimension getDefaultDimension_1_18_2() {
        return defaultDimension_1_18_2;
    }

    public Dimension getDimension_1_20_5() {
        return dimension_1_20_5;
    }

    public Dimension getDimension_1_21() {
        return dimension_1_21;
    }

    public Dimension getDimension_1_21_2() {
        return dimension_1_21_2;
    }

    public Dimension getDimension_1_21_4() {
        return dimension_1_21_4;
    }

    public CompoundBinaryTag getCodec_1_21_6() {
        return this.codec_1_21_6;
    }

    public CompoundBinaryTag getCodec_1_21_5() {
        return codec_1_21_5;
    }

    public CompoundBinaryTag getCodec_1_17() {
        return codec_1_17;
    }

    public CompoundBinaryTag getCodec_1_16_2() {
        return codec_1_16_2;
    }

    public Dimension getDimension_1_21_5() {
        return dimension_1_21_5;
    }

    public Dimension getDefaultDimension_1_17() {
        return defaultDimension_1_17;
    }

    public Dimension getDefaultDimension_1_16_2() {
        return defaultDimension_1_16_2;
    }

    public CompoundBinaryTag getTags_1_20_5() {
        return tags_1_20_5;
    }

    public CompoundBinaryTag getTags_1_21_5() {
        return tags_1_21_5;
    }

    public CompoundBinaryTag getTags_1_21_2() {
        return tags_1_21_2;
    }

    public CompoundBinaryTag getTags_1_21() {
        return tags_1_21;
    }

    public CompoundBinaryTag getTags_1_21_4() {
        return tags_1_21_4;
    }

    private Dimension getLegacyDimension(String def) {
        switch (def) {
            case "minecraft:overworld": {
                return new Dimension(0, def, null);
            }
            case "minecraft:the_nether": {
                return new Dimension(-1, def, null);
            }
            case "minecraft:the_end": {
                return new Dimension(1, def, null);
            }
            default: {
                Log.warning("Undefined dimension type: '%s'. Using 'minecraft:overworld' as default", def);
                return new Dimension(0, "minecraft:overworld", null);
            }
        }
    }

    private Dimension getModernDimension(String def, CompoundBinaryTag tag) {
        ListBinaryTag dimensions = tag.getCompound("minecraft:dimension_type").getList("value");

        for (int i = 0; i < dimensions.size(); i++) {
            CompoundBinaryTag dimension = (CompoundBinaryTag) dimensions.get(i);

            String name = dimension.getString("name");
            CompoundBinaryTag world = (CompoundBinaryTag) dimension.get("element");

            if (name.startsWith(def)) {
                return new Dimension(i, name, world);
            }
        }

        CompoundBinaryTag overWorld = (CompoundBinaryTag) ((CompoundBinaryTag) dimensions.get(0)).get("element");
        Log.warning("Undefined dimension type: '%s'. Using 'minecraft:overworld' as default", def);
        return new Dimension(0, "minecraft:overworld", overWorld);
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

    public CompoundBinaryTag getTags_1_21_6() {
        return tags_1_21_6;
    }

    public Dimension getDimension_1_21_6() {
        return dimension_1_21_6;
    }

    public CompoundBinaryTag getTags_1_21_7() {
        return tags_1_21_7;
    }

    public CompoundBinaryTag getCodec_1_21_7() {
        return codec_1_21_7;
    }
    /*private CompoundBinaryTag readSnbtFile(String resPath) throws IOException {
        InputStream in = DimensionRegistry.class.getClassLoader().getResourceAsStream(resPath);

        if (in == null)
            throw new FileNotFoundException("Cannot find snbt file " + resPath);

        var str = streamToString(in);
        //Log.error(str);
        return TagStringIO.get().asCompound(str);
    }

    private String streamToString(InputStream in) throws IOException {
        //return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        try (BufferedReader bufReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return bufReader.lines().collect(Collectors.joining("\n"));
        }
    }*/
}
