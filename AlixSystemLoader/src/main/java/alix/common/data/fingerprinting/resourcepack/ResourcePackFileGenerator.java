package alix.common.data.fingerprinting.resourcepack;

import alix.common.data.fingerprinting.resourcepack.file.VirtualFile;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static alix.common.data.fingerprinting.resourcepack.ResourcePackGenerator.setContents;

final class ResourcePackFileGenerator {

    final List<VirtualFile> rootFiles = new ArrayList<>();

    VirtualFile file(String name, String contents) {
        var file = this.file(name);
        setContents(file, contents);
        return file;
    }

    VirtualFile file(String name) {
        VirtualFile file = new VirtualFile();
        this.rootFiles.add(file);
        return this.file(file, name);
    }

    VirtualFile file(File parent, String name) {
        return this.file(new VirtualFile(parent), name);
    }

    VirtualFile file(VirtualFile parent, String name) {
        String actualName = name.replace('/', VirtualFile.separator);

        return new VirtualFile(parent, actualName);
    }

    VirtualFile file(VirtualFile parent, String name, String contents) {
        var file = this.file(parent, name);
        setContents(file, contents);
        return file;
    }

    private static String toUnicodeEscape(char c) {
        //Main.logInfo("C=" + (int) c);
        StringBuilder hex = new StringBuilder(Integer.toHexString(c));
        // Pad with leading zeros if needed to ensure 4 digits
        while (hex.length() < 4) hex.insert(0, "0");

        // Return the escape sequence
        return '\\' + "u" + hex.toString().toUpperCase();
    }

    private static String resource(String name) {
        try (var is = ResourcePackFileGenerator.class.getResourceAsStream(name)) {
            if (is == null)
                throw new RuntimeException("Resource " + name + " is null");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //https://minecraft.wiki/w/Pack_format#List_of_resource_pack_formats
    String packMcMetaJson(int bit) {
        return """
                {
                  "pack": {
                    "description": "Alix Device Key Bit %s",
                    "pack_format": 22,
                    "supported_formats": [1, 999]
                  }
                }
                """.formatted(bit);
    }
}