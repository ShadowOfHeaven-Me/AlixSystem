package alix.common.data.fingerprinting;

import alix.common.data.fingerprinting.resourcepack.ResourcePackGenerator;
import alix.common.data.fingerprinting.resourcepack.file.VirtualResourcePack;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FingerprintManager {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final int LEN = FingerprintBuilder.LEN;
    public static final String[] SHA1 = new String[LEN];
    public static final UUID[] BIT_UUIDS = new UUID[LEN];
    public static final Map<UUID, Integer> IDX_POS = new HashMap<>();

    static {
        try {
            for (int i = 0; i < LEN; i++) {
                VirtualResourcePack pack = ResourcePackGenerator.generate(i);
                String sha1 = pack.generateSha1();
                SHA1[i] = sha1;
                UUID uuid = UUID.nameUUIDFromBytes(("alix-fp-bit-" + i).getBytes(StandardCharsets.UTF_8));
                BIT_UUIDS[i] = uuid;
                IDX_POS.put(uuid, i);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer getIndex(UUID uuid) {
        return uuid != null ? IDX_POS.get(uuid) : null;
    }

    public static int generateNewFingerprint() {
        int val;
        do {
            val = SECURE_RANDOM.nextInt();
        } while (val == 0 || val == -1);
        return val;
    }
}