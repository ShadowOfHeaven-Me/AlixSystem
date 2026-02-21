package alix.common.data.fingerprinting;

import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerResourcePackSend;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.configuration.resourcepack.PacketConfigOutResourcePack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static alix.common.data.fingerprinting.FingerprintBuilder.LEN;
import static alix.common.data.fingerprinting.ResourcePackConstants.*;
import static alix.common.data.security.password.hashing.Hashing.SECURE_RANDOM;

public final class FingerprintManager {

    private static final String BAD_URL = "http://127.0.0.1:0";
    public static final PacketSnapshot NOT_LOADED_HIDER;
    public static final UUID NOT_LOADED_HIDER_UUID;
    public static final PacketSnapshot[] VALID_URLS;
    public static final PacketSnapshot[] INVALID_URLS;
    public static final Map<UUID, Integer> IDX_POS;

    static {
        VALID_URLS = new PacketSnapshot[LEN];
        INVALID_URLS = new PacketSnapshot[LEN];
        IDX_POS = new HashMap<>();
        for (int i = 0; i < LEN; i++) {
            var uuid = UUID.randomUUID();
            var url = URLS.get(i);
            var sha1 = SHA1.get(i);

            VALID_URLS[i] = of(uuid, url, sha1);
            INVALID_URLS[i] = of(uuid, BAD_URL, sha1);

            IDX_POS.put(uuid, i);
        }

        NOT_LOADED_HIDER_UUID = UUID.randomUUID();//NOT_LOADED_HIDER_URL
        NOT_LOADED_HIDER = of(NOT_LOADED_HIDER_UUID, BAD_URL, NOT_LOADED_HIDER_SHA1);
    }

    static Integer getIndex(UUID uuid) {
        return IDX_POS.get(uuid);
    }

    private static int generateNewFingerprint() {
        return SECURE_RANDOM.nextInt(1 << LEN);
    }

    private static PacketSnapshot of(UUID uuid, String url, String sha1) {
        return PacketSnapshot.of(new PacketConfigOutResourcePack(
                new WrapperConfigServerResourcePackSend(
                        uuid,
                        url,
                        sha1,
                        true,
                        null
                )));
    }
}