package alix.common.data.fingerprinting.resourcepack;

import alix.common.data.fingerprinting.resourcepack.file.VirtualFile;
import alix.common.data.fingerprinting.resourcepack.file.VirtualResourcePack;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.List;

public final class ResourcePackGenerator {

    public static VirtualResourcePack generate(int bit) {
        try {
            return new VirtualResourcePack(generate0(bit));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<VirtualFile> generate0(int bit) {
        var gen = new ResourcePackFileGenerator();
        gen.file("pack.mcmeta", gen.packMcMetaJson(bit));

        return gen.rootFiles;
    }

    static void copy(File src, VirtualFile dest) throws Exception {
        try (var in = Files.newInputStream(src.toPath())) {
            dest.contents = in.readAllBytes();
        }
    }

    static void copy(File src, File dest) throws Exception {
        dest.createNewFile();
        try (var in = Files.newInputStream(src.toPath());
             var out = Files.newOutputStream(dest.toPath())) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }
    }

    static void setContents(VirtualFile file, String contents) {
        file.contents = contents.getBytes();
    }

    static void setContents(File file, String contents) throws Exception {
        FileOutputStream os = new FileOutputStream(file);
        byte[] bytes = contents.getBytes();
        os.write(bytes);
        os.close();
    }
}