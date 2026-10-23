package alix.common.data.fingerprinting.resourcepack.file;

import java.security.MessageDigest;


import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class VirtualResourcePack {

    // Map to store file contents
    private final Map<String, byte[]> fileContents = new LinkedHashMap<>();

    public VirtualResourcePack(List<VirtualFile> files) {
        this.init(files);
    }

    private void init(List<VirtualFile> files) {
        if (files.isEmpty()) return;

        for (VirtualFile f : files) {
            this.init(f.parentOf());
            if (f.contents != null) this.addFile(f.path(), f.contents);
        }
    }

    /**
     * Add a file to the in-memory resource pack
     * @param filePath Path within the zip file (e.g., "pack.mcmeta" or "assets/minecraft/textures/item/diamond.png")
     * @param content Byte array of the file's content
     */
    public void addFile(String filePath, byte[] content) {
        fileContents.put(filePath, content);
    }

//    /**
//     * Create a zip file from the in-memory contents
//     * @param outputPath Path where the zip file will be written
//     */
//    public void writeToZip(String outputPath) throws IOException {
//        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputPath))) {
//            for (String filePath : fileContents.keySet()) {
//                ZipEntry zipEntry = new ZipEntry(filePath);
//                zipOut.putNextEntry(zipEntry);
//
//                zipOut.write(fileContents.get(filePath));
//                zipOut.closeEntry();
//            }
//        }
//    }
    /**
     * Get the resource pack as a byte array with improved ZIP entry handling
     */
    public byte[] toByteArray() throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {

            // Disable ZIP compression for image files to avoid potential issues
            //zipOut.setLevel(0); // 0 = no compression, store only

            for (Map.Entry<String, byte[]> entry : fileContents.entrySet()) {
                String filePath = entry.getKey();
                byte[] data = entry.getValue();

                // Create ZIP entry with normalized paths (always use forward slashes)
                ZipEntry zipEntry = new ZipEntry(filePath.replace('\\', '/'));

                //Minecraft is retarded, and does not account for the fact that images, in a compressed folder,
                // are also compressed

                // Set compression method explicitly based on file type
                if (filePath.endsWith(".png") || filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                    // Store images without compression (they're already compressed)
                    zipEntry.setMethod(ZipEntry.STORED);

                    // When using STORED method, we must set size and CRC explicitly
                    zipEntry.setSize(data.length);
                    zipEntry.setCrc(calculateCrc(data));
                    zipEntry.setCompressedSize(data.length);
                } else {
                    // Use default compression for other files
                    zipEntry.setMethod(ZipEntry.DEFLATED);
                }

                // Add the entry and write the data
                zipOut.putNextEntry(zipEntry);
                zipOut.write(data);
                zipOut.closeEntry();
            }

            zipOut.finish();
            return bos.toByteArray();
        }
    }
//    /**
//     * Get the resource pack as a byte array (for direct HTTP serving)
//     */
//    public byte[] toByteArray() throws Exception {
//        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
//             ZipOutputStream zipOut = new ZipOutputStream(bos)) {
//
//            for (Map.Entry<String, byte[]> entry : fileContents.entrySet()) {
//                var filePath = entry.getKey();
//                var data = entry.getValue();
//
//                ZipEntry zipEntry = new ZipEntry(filePath);
//                zipOut.putNextEntry(zipEntry);
//
//                zipOut.write(data);
//                zipOut.closeEntry();
//            }
//
//            zipOut.finish();
//            return bos.toByteArray();
//        }
//    }

    private long calculateCrc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    public String generateSha1() throws Exception {
        return calculateSha1(this.toByteArray());
    }

    private static String calculateSha1(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
