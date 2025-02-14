package alix.common.login.premium;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Optional;

public final class PremiumVerifier {

    public static final KeyPair keyPair = EncryptionUtil.generateKeyPair();

    public static boolean hasJoined(String username, String serverHash, InetAddress hostIp) throws IOException {
        String url;
        boolean isReverseProxyEnabled = true;
        if (isReverseProxyEnabled || hostIp instanceof Inet6Address) {
            url = String.format("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s", username, serverHash);
        } else {
            String encodedIP = URLEncoder.encode(hostIp.getHostAddress(), StandardCharsets.UTF_8);
            url = String.format("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s&ip=%s", username, serverHash, encodedIP);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();
        int responseCode = conn.getResponseCode();
        conn.disconnect();
        return responseCode != 204;
    }

    /**
     * @author games647 and FastLogin contributors
     */
    public static boolean verifyNonce(WrapperLoginClientEncryptionResponse packet,
                                      ClientPublicKey clientPublicKey, byte[] expectedToken, ServerVersion version) {
        try {
            if (version.isNewerThanOrEquals(ServerVersion.V_1_19)
                    && !version.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
                if (clientPublicKey == null) {
                    return EncryptionUtil.verifyNonce(expectedToken, keyPair.getPrivate(), packet.getEncryptedVerifyToken().orElseThrow());
                } else {
                    PublicKey publicKey = clientPublicKey.key();
                    Optional<SaltSignature> optSignature = packet.getSaltSignature();
                    if (optSignature.isEmpty()) {
                        return false;
                    }
                    SaltSignature signature = optSignature.get();

                    return EncryptionUtil.verifySignedNonce(expectedToken, publicKey, signature.getSalt(), signature.getSignature());
                }
            } else {
                byte[] nonce = packet.getEncryptedVerifyToken().orElseThrow();
                return EncryptionUtil.verifyNonce(expectedToken, keyPair.getPrivate(), nonce);
            }
        } catch (Exception signatureEx) {
            return false;
        }
    }

}