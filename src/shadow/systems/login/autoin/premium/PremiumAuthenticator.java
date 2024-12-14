package shadow.systems.login.autoin.premium;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumStatus;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.annotation.ScheduledForFix;
import alix.common.utils.other.throwable.AlixError;
import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketLoginReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.manager.server.ServerVersion;
import alix.libs.com.github.retrooper.packetevents.protocol.player.User;
import alix.libs.com.github.retrooper.packetevents.util.crypto.SaltSignature;
import alix.libs.com.github.retrooper.packetevents.util.crypto.SignatureData;
import alix.libs.com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;
import alix.libs.com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import alix.libs.com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.login.autoin.PremiumAccountCache;
import shadow.systems.login.autoin.PremiumSetting;
import shadow.systems.login.autoin.PremiumUtils;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.netty.NettyUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static shadow.systems.login.autoin.premium.AuthReflection.cipherMethod;
import static shadow.systems.login.autoin.premium.AuthReflection.encryptMethod;

//https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/PaperListeners.java

public final class PremiumAuthenticator {

    //todo: add support
    private final boolean isReverseProxyEnabled = true;

    private static final ByteBuf
            illegalEncryptionState = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cIllegal encryption state"),
            invalidNonce = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cInvalid nonce"),
            cannotDecryptSharedSecret = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cCannot decrypt shared secret"),
            invalidSession = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cInvalid session"),
            cannotVerifySession = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cCannot verify session"),
            internalErrorEncryption = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cInternal error (Encryption)"),
            couldNotEnableEncryption = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cCouldn't enable encryption");

    //with caffeine
    @OptimizationCandidate
    private final Cache<String, EncryptionData> encryptionDataCache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    private final SecureRandom random = new SecureRandom();
    private final KeyPair keyPair = EncryptionUtil.generateKeyPair();

    private void onEncryptionResponse(User user, WrapperLoginClientEncryptionResponse packet) {
        byte[] sharedSecret = packet.getEncryptedSharedSecret();

        EncryptionData data = encryptionDataCache.getIfPresent(user.getAddress().toString());

        if (data == null) {
            this.disconnectWith(user, illegalEncryptionState);
            return;
        }

        byte[] expectedToken = data.token().clone();

        if (!verifyNonce(packet, data.publicKey(), expectedToken)) {
            this.disconnectWith(user, invalidNonce);
            return;//did they forget a return here? - https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/PaperListeners.java#L270
        }

        //Verify session
        PrivateKey privateKey = keyPair.getPrivate();

        SecretKey loginKey;

        try {
            loginKey = EncryptionUtil.decryptSharedKey(privateKey, sharedSecret);
        } catch (Exception securityEx) {
            this.disconnectWith(user, cannotDecryptSharedSecret);
            return;
        }

        try {
            if (!enableEncryption(loginKey, user, user.getChannel())) {
                return;
            }
        } catch (Exception e) {
            this.disconnectWith(user, cannotDecryptSharedSecret);
            return;
        }

        String serverId = EncryptionUtil.getServerIdHashString("", loginKey, keyPair.getPublic());
        String username = data.username();
        InetSocketAddress address = user.getAddress();

        try {
            if (hasJoined(username, serverId, address.getAddress())) {
                //Main.logError("IZZA PREMIUMMMMM");
                //we don't need to do anything here, since the PersistentUserData (or the PremiumAccountCache) was already updated, and the player is kicked if he doesn't pass the premium check
                receiveFakeStartPacket(username, data.publicKey(), user.getChannel(), data.uuid());
            } else {
                this.disconnectWith(user, invalidSession);
            }
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                Main.logWarning("Session verification timed out (5 seconds) for " + username);
            }
            this.disconnectWith(user, cannotVerifySession);
        }
    }

    //private final boolean newerT

    private ClientPublicKey getClientKey(WrapperLoginClientLoginStart packet) {
        if (getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            return null;
        } else {
            Optional<SignatureData> signature = packet.getSignatureData();

            return signature.isPresent() ? signature.map(_data -> {
                Instant expires = _data.getTimestamp();
                PublicKey key = _data.getPublicKey();
                byte[] signatureData = _data.getSignature();

                return new ClientPublicKey(expires, key, signatureData);
            }).get() : null;
        }
    }

    private void onLoginStart(User user, WrapperLoginClientLoginStart packet) {
        //Main.logError("LOGIN START: " + AlixUtils.getFields(packet) + " " + packet.getPlayerUUID().get().version());

        String sessionKey = user.getAddress().toString();
        String name = packet.getUsername();
        UUID uuid = packet.getPlayerUUID().orElse(null);
        //Main.logError("LOGIN START: " + AlixUtils.getFields(packet) + " VARIANT " + uuid.variant() + " ");
        Channel channel = (Channel) user.getChannel();

        encryptionDataCache.invalidate(sessionKey);

        if (Dependencies.isFloodgatePresent) {
            // don't continue execution if the player was kicked by Floodgate
            if (!FloodgateHelper.processFloodgateTasks(channel)) return;
        }

        ClientPublicKey clientKey = this.getClientKey(packet);

        //Alix - start
        PersistentUserData data = UserFileManager.get(name);

        /*boolean justCreatedPremiumData = false;

        boolean performPremiumCheck = data == null ?
                (justCreatedPremiumData =
                        (data = performPremiumCheckNullData(name, user.getAddress().getAddress())) != null)
                : performPremiumCheck(data, name);*/

        PremiumData newPremiumData;
        boolean performPremiumCheck;// = data != null ? performPremiumCheck(data) : performPremiumCheckNullData(name);

        if (data != null) {
            performPremiumCheck = performPremiumCheck(data, uuid, clientKey);
            newPremiumData = data.getPremiumData();
        } else {
            newPremiumData = performPremiumCheckNullData(name, uuid, clientKey);
            performPremiumCheck = newPremiumData.getStatus().isPremium();
        }

        //Main.logError("IS PREMIUM: " + performPremiumCheck + " DATA: " + newPremiumData.premiumUUID() + " STORED PREMIUM: " + newPremiumData.getStatus().isPremium());

        if (!AlixChannelHandler.onLoginStart(user, name, data, performPremiumCheck)) return;
        //Alix - end

        if (Dependencies.isBedrock(channel)) {
            //Floodgate player, do not handle, only retransmit the packet. The UUID will be set by Floodgate
            receiveFakeStartPacket(name, clientKey, channel, uuid);
            return;
        }
        //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/listener/AuthenticListeners.java#L29

        if (performPremiumCheck) {
            try {
                //should never happen
                if (newPremiumData.premiumUUID() == null) throw new AlixError("Null uuid during encryption send");

                byte[] token = EncryptionUtil.generateVerifyToken(random);

                WrapperLoginServerEncryptionRequest newPacket = new WrapperLoginServerEncryptionRequest("", keyPair.getPublic(), token);
                encryptionDataCache.put(sessionKey, new EncryptionData(name, token, clientKey, newPremiumData.premiumUUID()));

                //PacketEvents.getAPI().getProtocolManager().sendPacket(user.getChannel(), newPacket);
                NettyUtils.getSilentContext(channel).writeAndFlush(NettyUtils.dynamic(newPacket));
            } catch (Exception e) {
                this.disconnectWith(user, internalErrorEncryption);
                Main.logError("Failed to send encryption begin packet for player " + name + "! Kicking player.");
                e.printStackTrace();
            }
        } else {
            // The original event has been cancelled, so we need to send a fake start packet.
            receiveFakeStartPacket(name, clientKey, channel, uuid);
        }
    }

    private PremiumData performPremiumCheckNullData(String name, UUID uuid, ClientPublicKey clientPublicKey) {
        PremiumData cache = PremiumAccountCache.get(name);
        if (cache != null) return cache;

        if (!PremiumSetting.requirePremium(false, uuid, clientPublicKey)) return PremiumData.UNKNOWN;
        PremiumData newData = PremiumUtils.getPremiumData(name);
        PremiumStatus status = newData.getStatus();

        if (status.isUnknown()) return PremiumData.UNKNOWN;//fallback

        //cache the username's premium status when it isn't unknown
        PremiumAccountCache.add(name, newData);
        return newData;
    }

    private boolean performPremiumCheck(PersistentUserData data, UUID uuid, ClientPublicKey clientPublicKey) {
        switch (data.getPremiumData().getStatus()) {
            case PREMIUM:
                return true;
            case NON_PREMIUM:
                return false;
            case UNKNOWN:
                if (!PremiumSetting.requirePremium(data, uuid, clientPublicKey)) return false;
                PremiumData newData = PremiumUtils.getPremiumData(data.getName());

                if (newData.getStatus().isUnknown()) return false;//fallback
                data.setPremiumData(newData);
                return newData.getStatus().isPremium();
            default:
                throw new AlixError("Invalid: " + data.getPremiumData().getStatus());
        }
    }

    private void asyncPacketReceive(PacketLoginReceiveEvent event) {
        User user = event.getUser();

        if (AlixUtils.isDebugEnabled)
            Main.logDebug("Packet received " + event.getPacketType() + " from " + user.getName() + " (" + user.getAddress().toString() + ")");

        switch (event.getPacketType()) {
            case LOGIN_START: {
                this.onLoginStart(user, new WrapperLoginClientLoginStart(event));
                return;
            }
            case ENCRYPTION_RESPONSE: {
                this.onEncryptionResponse(user, new WrapperLoginClientEncryptionResponse(event));
            }
        }
    }

    //AlixScheduler.async usage might not be ideal here
    @ScheduledForFix
    @OptimizationCandidate
    private void async(Runnable r) {
        AlixScheduler.async(r);
    }

    public void onLoginStartPacketReceive(PacketLoginReceiveEvent event) {
        event.setCancelled(true);
        PacketLoginReceiveEvent copy = event.clone();

        this.async(() -> {
            try {
                asyncPacketReceive(copy);
            } finally {
                copy.cleanUp();
            }
        });
    }

    /**
     * fake a new login packet in order to let the server handle all the other stuff
     *
     * @author games647 and FastLogin contributors
     */
    private void receiveFakeStartPacket(String username, ClientPublicKey clientKey, Object channel, UUID uuid) {
        WrapperLoginClientLoginStart startPacket;
        if (getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20)) {
            startPacket = new WrapperLoginClientLoginStart(getServerVersion().toClientVersion(),
                    username, clientKey == null ? null : clientKey.toSignatureData(), uuid);
        } else if (getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19)) {
            startPacket = new WrapperLoginClientLoginStart(getServerVersion().toClientVersion(), username, clientKey == null ? null : clientKey.toSignatureData());
        } else {
            startPacket = new WrapperLoginClientLoginStart(getServerVersion().toClientVersion(), username);
        }
        /*WrapperLoginClientLoginStart startPacket = new WrapperLoginClientLoginStart(getServerVersion().toClientVersion(),
                username, clientKey == null ? null : clientKey.toSignatureData(), uuid);*/
        PacketEvents.getAPI().getProtocolManager().receivePacketSilently(channel, startPacket);
    }

    private ServerVersion getServerVersion() {
        return PacketEvents.getAPI().getServerManager().getVersion();
    }

    public boolean hasJoined(String username, String serverHash, InetAddress hostIp) throws IOException {
        String url;
        if (hostIp instanceof Inet6Address || isReverseProxyEnabled) {
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
    private boolean enableEncryption(SecretKey loginKey, User user, Object channel) throws IllegalArgumentException {

        try {
            Object networkManager = AuthReflection.findNetworkManager(channel);

            // If cipherMethod is null - use old encryption (pre MC 1.16.4), otherwise use the new cipher one
            if (cipherMethod == null) {
                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, loginKey);
            } else {
                // Create ciphers from login key
                Object decryptionCipher = cipherMethod.invoke(null, Cipher.DECRYPT_MODE, loginKey);
                Object encryptionCipher = cipherMethod.invoke(null, Cipher.ENCRYPT_MODE, loginKey);

                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, decryptionCipher, encryptionCipher);
            }
        } catch (Exception ex) {
            this.disconnectWith(user, couldNotEnableEncryption);
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private void disconnectWith(User user, ByteBuf constDisconnectBuf) {
        NettyUtils.closeAfterConstSend((Channel) user.getChannel(), constDisconnectBuf);
    }

/*    @OptimizationCandidate
    private void kickPlayer(String reason, User user) {
        MethodProvider.kickAsyncLoginDynamic((Channel) user.getChannel(), reason);
    }*/

    /**
     * @author games647 and FastLogin contributors
     */
    private boolean verifyNonce(WrapperLoginClientEncryptionResponse packet,
                                ClientPublicKey clientPublicKey, byte[] expectedToken) {
        try {
            if (getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19)
                    && !getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
                if (clientPublicKey == null) {
                    return EncryptionUtil.verifyNonce(expectedToken, keyPair.getPrivate(), packet.getEncryptedVerifyToken().get());
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
                byte[] nonce = packet.getEncryptedVerifyToken().get();
                return EncryptionUtil.verifyNonce(expectedToken, keyPair.getPrivate(), nonce);
            }
        } catch (Exception signatureEx) {
            return false;
        }
    }
}