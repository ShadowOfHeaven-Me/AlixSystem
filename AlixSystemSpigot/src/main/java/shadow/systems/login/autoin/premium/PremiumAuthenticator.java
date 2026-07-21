package shadow.systems.login.autoin.premium;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.VerifiedCache;
import alix.common.login.premium.*;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCache;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.simple.PacketLoginReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.login.autoin.uuid.PremiumUuidSetting;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.netty.NettyUtils;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.integration.LimboIntegration;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static shadow.systems.login.autoin.premium.AuthReflection.cipherMethod;
import static shadow.systems.login.autoin.premium.AuthReflection.encryptMethod;
import static shadow.utils.main.AlixUtils.ONLINE_MODE;

//https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/PaperListeners.java

public final class PremiumAuthenticator {

    private static final PacketSnapshot
            illegalEncryptionState = disconnect("premium-disconnect-illegal-encryption-state"),
            invalidNonce = disconnect("premium-disconnect-invalid-nonce"),
            cannotDecryptSharedSecret = disconnect("premium-disconnect-cannot-decrypt-secret"),
            invalidSession = disconnect("premium-disconnect-invalid-session"),
            cannotVerifySession = disconnect("premium-disconnect-cannot-verify-session"),
            internalErrorEncryption = disconnect("premium-disconnect-internal-error"),
            couldNotEnableEncryption = disconnect("premium-disconnect-cannot-enable-encryption");

    private final Map<User, EncryptionData> encryptionDataCache = AlixCache.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS).weakKeys()
            .<User, EncryptionData>build().asMap();

    private final KeyPair keyPair = PremiumVerifier.keyPair;
    private final boolean
            assumeNonPremiumOnFailedAuth = Main.config.getBoolean("assume-non-premium-if-auth-failed"),
            __noPremiumAuthButKeepIdentity = AlixUtils.__noPremiumAuthButKeepIdentity;

    private static PacketSnapshot disconnect(String msgId) {
        return PacketLoginDisconnect.snapshot(Messages.get(msgId));
    }

    private void onInvalidAuth(User user, ClientPublicKey publicKey, PacketSnapshot disconnectReason) {
        String name = user.getName();
        PersistentUserData data = UserFileManager.get(name);
        boolean registered = PersistentUserData.isRegistered(data);

        if (!registered && assumeNonPremiumOnFailedAuth) {
            Main.logInfo("Assuming the premium-nicknamed user " + name + " is non-premium");

            if (data != null) data.setPremiumData(PremiumData.NON_PREMIUM);
            else PremiumDataCache.add(name, PremiumData.NON_PREMIUM);

            receiveFakeStartPacket(name, publicKey, (Channel) user.getChannel(), user.getUUID());
            return;
        }
        this.disconnectWith(user, disconnectReason);
    }

    private void onEncryptionResponse(User user, WrapperLoginClientEncryptionResponse packet) {
        if (ONLINE_MODE)
            return;

        byte[] sharedSecret = packet.getEncryptedSharedSecret();

        EncryptionData data = this.encryptionDataCache.remove(user);

        if (data == null) {
            //we weren't expecting this packet
            this.disconnectWith(user, illegalEncryptionState);
            return;
        }

        byte[] expectedToken = data.token();
        PrivateKey privateKey = keyPair.getPrivate();

        SecretKey loginKey;

        try {
            loginKey = EncryptionUtil.decryptSharedKey(privateKey, sharedSecret);
        } catch (Exception securityEx) {
            //we cannot enable encryption without this one
            this.disconnectWith(user, cannotDecryptSharedSecret);
            return;
        }

        var channel = (Channel) user.getChannel();
        AuthReflection.findNetworkManager(channel, networkManager -> {
            //enable encryption once we know that the player is expecting the connection to be encrypted
            if (!enableEncryption(loginKey, user, networkManager))
                return;

            if (!verifyNonce(packet, data.publicKey(), expectedToken)) {
                if (!data.shouldAuthenticate()) {
                    //wtf do we do now
                    this.disconnectWith(user, invalidNonce);
                    return;
                }

                this.onInvalidAuth(user, data.publicKey(), invalidNonce);
                return;//did they forget a return here? - https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/PaperListeners.java#L270
            }

            String packetUsername = data.packetUsername();
            String serverUsername = data.serverUsername();
            InetSocketAddress address = user.getAddress();

            //Main.debug("data.shouldAuthenticate()=" + data.shouldAuthenticate());

            if (!data.shouldAuthenticate()) {
                receiveFakeStartPacket(packetUsername, data.publicKey(), channel, data.uuid());
                return;
            }

            //Verify session

            String serverId = EncryptionUtil.getServerIdHashString("", loginKey, keyPair.getPublic());

            AlixScheduler.asyncBlocking(() -> {
                try {
                    //make sure to remove the name char prefix
                    if (hasJoined(PremiumUtils.getNonPrefixedName(packetUsername), serverId, address.getAddress())) {

                        if (PremiumUuidSetting.CONFIG.shouldAssignPremiumUuid(serverUsername, data.uuid()))
                            this.setPremiumUUID(networkManager, data.uuid());

                        //Main.debug("VerifiedCache.verify=" + user.getName());
                        VerifiedCache.verify(serverUsername, user);
                        receiveFakeStartPacket(packetUsername, data.publicKey(), channel, data.uuid());
                    } else {
                        this.onInvalidAuth(user, data.publicKey(), invalidSession);
                    }
                } catch (IOException e) {
                    if (e instanceof SocketTimeoutException) {
                        Main.logWarning("Session verification timed out (5 seconds) for " + serverUsername);
                    }
                    //todo: see if this correct
                    this.onInvalidAuth(user, data.publicKey(), cannotVerifySession);
                }
            });
        });
    }

    private void onLoginStart(User user, WrapperLoginClientLoginStart packet) {
        Channel channel = (Channel) user.getChannel();
        String packetUsername = packet.getUsername();
        String serverUsername = Dependencies.getCorrectUsername(channel, packetUsername);
        UUID uuid = packet.getPlayerUUID().orElse(null);

        user.getProfile().setName(serverUsername);
        user.getProfile().setUUID(uuid);
        ClientVersion version = user.getClientVersion();

        if (Dependencies.isFloodgatePresent) {
            // don't continue execution if the player was kicked by Floodgate
            if (!FloodgateHelper.isJoinAllowed(channel)) return;
        }

        ClientPublicKey clientKey = ClientPublicKey.createKey(packet.getSignatureData().orElse(null));

        PersistentUserData data = UserFileManager.get(serverUsername);

        Consumer<PremiumData> consumer = newPremiumData ->
                this.onLoginStart0(user, data, channel, packetUsername, serverUsername, clientKey, uuid, newPremiumData);

        if (__noPremiumAuthButKeepIdentity)
            consumer.accept(PremiumData.UNKNOWN);
        else if (data != null)
            PremiumSetting.performPremiumCheck(channel, data, packetUsername, uuid, version, consumer);
        else
            PremiumSetting.performPremiumCheckNullData(channel, packetUsername, uuid, version, consumer);
    }

    private void onLoginStart0(User user, PersistentUserData data, Channel channel, String packetUsername, String serverUsername,
                               ClientPublicKey clientKey, UUID uuid, PremiumData newPremiumData) {
        boolean performPremiumCheck = newPremiumData.getStatus().isPremium();

        if (data != null || LimboIntegration.hasCompletedCaptcha(channel, packetUsername)) {
            //Since AlixChannelHandler.onLoginStart will not be invoked, add the connecting user manually
            if (!AlixChannelHandler.putConnecting(user, serverUsername)) return;

            //If hasn't passed the limbo captcha - either limbo is disabled or the player failed the captcha (so this isn't a reconnect after the limbo pass), so invoke onLoginStart here
        } else {
            Main.logWarning("onLoginStart0 invoked without captcha pass for " + serverUsername + "! Disconnecting him for safety!");
            NettyUtils.closeAfterDynamicSend(channel, OutDisconnectPacketConstructor.constructDynamicAtLoginPhase("§c[No captcha]"));
            return;
        }

        if (Dependencies.isBedrock(channel)) {
            //Floodgate player, do not handle, only retransmit the packet. The UUID will be set by Floodgate
            receiveFakeStartPacket(packetUsername, clientKey, channel, uuid);
            return;
        }
        //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/listener/AuthenticListeners.java#L29

        if (__noPremiumAuthButKeepIdentity) {
            PremiumData premiumData = PersistentUserData.getPremiumData(data);

            if (premiumData.getStatus().isPremium()) {
                AuthReflection.findNetworkManager((Channel) user.getChannel(), networkManager -> {
                    this.setPremiumUUID(networkManager, premiumData.premiumUUID());
                    Main.logError("setPremiumUUID=" + premiumData.premiumUUID());
                    receiveFakeStartPacket(packetUsername, clientKey, channel, uuid);
                });
                return;
            }

            receiveFakeStartPacket(packetUsername, clientKey, channel, uuid);
            return;
        }

        boolean encrypt = !ONLINE_MODE && (performPremiumCheck || EncryptionSetting.enableEncryption(user.getClientVersion()));

        if (encrypt) {
            try {
                //should never happen
                if (performPremiumCheck && newPremiumData.premiumUUID() == null)
                    throw new AlixError("Null uuid during encryption send");

                byte[] token = EncryptionUtil.generateVerifyToken();

                //the player should auth if the encryption request stems from premium auth req
                boolean shouldAuthenticate = performPremiumCheck;
                WrapperLoginServerEncryptionRequest newPacket = new WrapperLoginServerEncryptionRequest("", keyPair.getPublic(), token, shouldAuthenticate);
                var dataUuid = shouldAuthenticate ? newPremiumData.premiumUUID() : uuid;
                this.encryptionDataCache.put(user, new EncryptionData(packetUsername, serverUsername, token, clientKey, dataUuid, shouldAuthenticate));

                ByteBuf dynamicWrapper = NettyUtils.dynamic(newPacket);
                ChannelHandlerContext silentCtx = NettyUtils.getSilentContext(channel);
                channel.eventLoop().execute(() -> {
                    silentCtx.write(dynamicWrapper);
                    //workaround to flush our current packet when FlushConsolidationHandler disallows to do so
                    channel.unsafe().flush();
                });
            } catch (Exception e) {
                this.disconnectWith(user, internalErrorEncryption);
                Main.logError("Failed to send encryption begin packet for player " + packetUsername + "! Kicking player.");
                e.printStackTrace();
            }
        } else {
            // The original event has been cancelled, so we need to send a fake start packet.
            receiveFakeStartPacket(packetUsername, clientKey, channel, uuid);
        }
    }

    private void packetReceive0(User user, PacketWrapper<?> wrapper, PacketType.Login.Client type) {
        switch (type) {
            case LOGIN_START: {
                this.onLoginStart(user, (WrapperLoginClientLoginStart) wrapper);
                return;
            }
            case ENCRYPTION_RESPONSE: {
                //Log.error("ENCRYPTION ENABLED=" + SpigotEncryption.isOnlineEncryptionEnabled((Channel) user.getChannel()));
                this.onEncryptionResponse(user, (WrapperLoginClientEncryptionResponse) wrapper);
            }
        }
    }

    public void onPacketReceive(PacketLoginReceiveEvent event) {
        event.setCancelled(true);
        var type = event.getPacketType();
        var wrapper = switch (type) {
            case LOGIN_START -> new WrapperLoginClientLoginStart(event);
            case ENCRYPTION_RESPONSE -> new WrapperLoginClientEncryptionResponse(event);
            default -> throw new AlixError();
        };

        packetReceive0(event.getUser(), wrapper, type);
    }

    void receiveFakeStartPacket(String username, ClientPublicKey clientKey, Channel channel, UUID uuid) {
        if (channel.eventLoop().inEventLoop())
            this.receiveFakeStartPacket0(username, clientKey, channel, uuid);
        else
            channel.eventLoop().execute(() -> this.receiveFakeStartPacket0(username, clientKey, channel, uuid));
    }

    /**
     * fake a new login packet in order to let the server handle all the other stuff
     *
     * @author games647 and FastLogin contributors
     */
    private void receiveFakeStartPacket0(String username, ClientPublicKey clientKey, Channel channel, UUID uuid) {
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
        return PremiumVerifier.hasJoined(username, serverHash, hostIp);
    }

    /**
     * @author games647 and FastLogin contributors
     */
    private boolean enableEncryption(SecretKey loginKey, User user, Object networkManager) throws IllegalArgumentException {
        try {

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

    private void setPremiumUUID(Object networkManager, UUID premiumUUID) {
        if (premiumUUID == null)
            throw new AlixError("premiumUUID is null! Report this immediately!");
        AuthReflection.setUUID(networkManager, premiumUUID);
    }

    private void disconnectWith(User user, PacketSnapshot packet) {
        var channel = (Channel) user.getChannel();
        var cipher = CipherHandler.encryptionFor(channel);

        //AlixCommonMain.logWarning("PIPELINE= " + channel.pipeline().names());
        //new Exception().printStackTrace();
        if (channel.eventLoop().inEventLoop())
            PacketUtils.closeWith(user, packet, cipher);
        else
            channel.eventLoop().execute(() -> PacketUtils.closeWith(user, packet, cipher));
        //NettyUtils.closeAfterConstSend(, constDisconnectBuf);
    }

    private boolean verifyNonce(WrapperLoginClientEncryptionResponse packet,
                                ClientPublicKey clientPublicKey, byte[] expectedToken) {
        //TODO: is getServerVersion() correct?
        return PremiumVerifier.verifyNonce(packet, clientPublicKey, expectedToken, this.getServerVersion());
    }
}