package alix.common.data.fingerprinting;

import alix.common.data.fingerprinting.resourcepack.ResourcePackGenerator;
import alix.common.data.fingerprinting.resourcepack.file.VirtualResourcePack;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TesterFingerprinting {

    @Test
    public void testFingerprinting() throws Exception {
        // 1. Test FingerprintManager constants & generation
        Assertions.assertEquals(32, FingerprintManager.LEN);
        Assertions.assertEquals(32, FingerprintManager.BIT_UUIDS.length);
        Assertions.assertEquals(32, FingerprintManager.SHA1.length);

        for (int i = 0; i < 32; i++) {
            UUID uuid = FingerprintManager.BIT_UUIDS[i];
            Integer idx = FingerprintManager.getIndex(uuid);
            Assertions.assertNotNull(idx, "Index lookup failed for bit " + i);
            Assertions.assertEquals(i, idx.intValue());
            Assertions.assertNotNull(FingerprintManager.SHA1[i]);
            Assertions.assertFalse(FingerprintManager.SHA1[i].isEmpty());
        }
        Assertions.assertNull(FingerprintManager.getIndex(UUID.randomUUID()));

        // 2. Test candidate fingerprint generation: must never be 0 or -1 (32x 1)
        for (int i = 0; i < 100_000; i++) {
            int candidate = FingerprintManager.generateNewFingerprint();
            Assertions.assertNotEquals(0, candidate, "Generated 0 as fingerprint candidate");
            Assertions.assertNotEquals(-1, candidate, "Generated -1 (32x 1) as fingerprint candidate");
        }

        // 3. Test ResourcePackGenerator & VirtualResourcePack ZIP structure
        for (int i = 0; i < 32; i++) {
            VirtualResourcePack pack = ResourcePackGenerator.generate(i);
            byte[] bytes = pack.toByteArray();
            Assertions.assertNotNull(bytes);
            Assertions.assertTrue(bytes.length > 0);

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
                ZipEntry entry;
                boolean foundPackMeta = false;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("pack.mcmeta")) {
                        foundPackMeta = true;
                        String jsonStr = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                        Assertions.assertTrue(jsonStr.contains("\"pack_format\": 22"));
                        Assertions.assertTrue(jsonStr.contains("Bit " + i));
                    }
                }
                Assertions.assertTrue(foundPackMeta, "pack.mcmeta not found in bit " + i + " zip");
            }
        }

        // 4. Test Bit Mask & Detection Logic
        // Scenario A: Client requested no downloads (all 32 cached)
        int downloadRequestedMaskA = 0;
        int detectedFpA = ~downloadRequestedMaskA;
        Assertions.assertEquals(-1, detectedFpA, "Expected -1 for all cached");

        // Scenario B: Client requested all downloads (fresh device)
        int downloadRequestedMaskB = -1;
        int detectedFpB = ~downloadRequestedMaskB;
        Assertions.assertEquals(0, detectedFpB, "Expected 0 for fresh device");

        // Scenario C: Client requested downloads for odd bits, even bits are cached
        int downloadRequestedMaskC = 0;
        for (int i = 0; i < 32; i++) {
            if (i % 2 != 0) { // odd bit requested download
                downloadRequestedMaskC |= (1 << i);
            }
        }
        int detectedFpC = ~downloadRequestedMaskC;
        for (int i = 0; i < 32; i++) {
            boolean cached = (detectedFpC & (1 << i)) != 0;
            if (i % 2 == 0) {
                Assertions.assertTrue(cached, "Expected bit " + i + " to be cached (1)");
            } else {
                Assertions.assertFalse(cached, "Expected bit " + i + " to NOT be cached (0)");
            }
        }
    }
}
