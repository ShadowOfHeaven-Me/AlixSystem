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
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
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
import com.google.common.cache.Cache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import shadow.Main;
import shadow.systems.dependencies.Dependencies;
import shadow.systems.netty.AlixChannelHandler;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.netty.NettyUtils;
import ua.nanit.limbo.integration.LimboIntegration;

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

import static shadow.systems.login.autoin.premium.AuthReflection.cipherMethod;
import static shadow.systems.login.autoin.premium.AuthReflection.encryptMethod;

//https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/PaperListeners.java

public final class PremiumAuthenticator {

    //todo: add support
    //private final boolean isReverseProxyEnabled = true;

    private static final ByteBuf
            illegalEncryptionState = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("premium-disconnect-illegal-encryption-state")),
            invalidNonce = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("premium-disconnect-invalid-nonce")),
            cannotDecryptSharedSecret = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("premium-disconnect-cannot-decrypt-secret")),
            invalidSession = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("premium-disconnect-invalid-session")),
            cannotVerifySession = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("premium-disconnect-cannot-verify-session")),
            internalErrorEncryption = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("premium-disconnect-internal-error")),
            couldNotEnableEncryption = OutDisconnectPacketConstructor.constructConstAtLoginPhase(Messages.get("premium-disconnect-cannot-enable-encryption"));
    //cachedNameDoesNotMatch = OutDisconnectPacketConstructor.constructConstAtLoginPhase("§cCached name does not match");

    //with caffeine
    @OptimizationCandidate
    private final Map<User, EncryptionData> encryptionDataCache;

    {
        Cache<User, EncryptionData> cache = AlixCache.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS).weakKeys()
                .build();
        this.encryptionDataCache = cache.asMap();
    }


    private final KeyPair keyPair = PremiumVerifier.keyPair;
    private final boolean
            assumeNonPremiumOnFailedAuth = Main.config.getBoolean("assume-non-premium-if-auth-failed"),
            assignPremiumUUID = Main.config.getBoolean("premium-uuid");

    private void onInvalidAuth(User user, ClientPublicKey publicKey, ByteBuf disconnectReason) {
        String name = user.getName();
        PersistentUserData data = UserFileManager.get(name);
        boolean registered = PersistentUserData.isRegistered(data);

        if (!registered && assumeNonPremiumOnFailedAuth) {
            Main.logInfo("Assuming the premium-nicknamed user " + name + " is non-premium!");

            if (data != null) data.setPremiumData(PremiumData.NON_PREMIUM);
            else PremiumDataCache.add(name, PremiumData.NON_PREMIUM);

            receiveFakeStartPacket(name, publicKey, user.getChannel(), user.getUUID());
            return;
        }
        this.disconnectWith(user, disconnectReason);
    }

    private void onEncryptionResponse(User user, WrapperLoginClientEncryptionResponse packet) {
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

        //enable encryption once we know that the player is expecting the connection to be encrypted
        Object networkManager = AuthReflection.findNetworkManager((Channel) user.getChannel());

        if (!enableEncryption(loginKey, user, networkManager))
            return;

        if (!verifyNonce(packet, data.publicKey(), expectedToken)) {
            this.onInvalidAuth(user, data.publicKey(), invalidNonce);
            return;//did they forget a return here? - https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/PaperListeners.java#L270
        }

        //Verify session

        String serverId = EncryptionUtil.getServerIdHashString("", loginKey, keyPair.getPublic());
        String packetUsername = data.packetUsername();
        String serverUsername = data.serverUsername();
        InetSocketAddress address = user.getAddress();

        try {
            //make sure to remove the name char prefix
            if (hasJoined(PremiumUtils.getNonPrefixedName(packetUsername), serverId, address.getAddress())) {

                if (this.assignPremiumUUID)
                    this.setPremiumUUID(networkManager, data.premiumUUID());

                VerifiedCache.verify(serverUsername, user);
                receiveFakeStartPacket(packetUsername, data.publicKey(), user.getChannel(), data.premiumUUID());
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
    }

    private void onLoginStart(User user, WrapperLoginClientLoginStart packet) {
        //Main.logError("LOGIN START: " + AlixUtils.getFields(packet) + " " + packet.getPlayerUUID().get().version());

        Channel channel = (Channel) user.getChannel();
        InetAddress address = user.getAddress().getAddress();
        //String sessionKey = user.getAddress().toString();
        String packetUsername = packet.getUsername();
        //Main.logInfo("PACKET USERNAME: " + packetUsername);
        String bedrockName = Dependencies.getCorrectUsername(channel, packetUsername);
        UUID uuid = packet.getPlayerUUID().orElse(null);
        //Main.debug("packetUsername: '" + packetUsername + "' bedrockName: '" + bedrockName + "'");

        user.getProfile().setName(bedrockName);
        user.getProfile().setUUID(uuid);
        //Main.logError("LOGIN START: " + AlixUtils.getFields(packet) + " VARIANT " + uuid.variant() + " ");
        ClientVersion version = user.getClientVersion();

        //encryptionDataCache.remove(sessionKey);

        if (Dependencies.isFloodgatePresent) {
            // don't continue execution if the player was kicked by Floodgate
            if (!FloodgateHelper.processFloodgateTasks(channel)) return;
        }

        ClientPublicKey clientKey = ClientPublicKey.createKey(packet.getSignatureData().orElse(null));

        //Alix - start
        PersistentUserData data = UserFileManager.get(bedrockName);

        /*boolean justCreatedPremiumData = false;

        boolean performPremiumCheck = data == null ?
                (justCreatedPremiumData =
                        (data = performPremiumCheckNullData(name, user.getAddress().getAddress())) != null)
                : performPremiumCheck(data, name);*/

        PremiumData newPremiumData;
        boolean performPremiumCheck;// = data != null ? performPremiumCheck(data) : performPremiumCheckNullData(name);

        if (data != null) {
            performPremiumCheck = PremiumSetting.performPremiumCheck(data, packetUsername, uuid, clientKey, version);
            newPremiumData = data.getPremiumData();
        } else {
            newPremiumData = PremiumSetting.performPremiumCheckNullData(packetUsername, uuid, clientKey, version);
            performPremiumCheck = newPremiumData.getStatus().isPremium();
        }

        //Main.logError("IS PREMIUM: " + performPremiumCheck + " DATA: " + newPremiumData.getStatus() + " suggestsStatus: " + PremiumUtils.suggestsStatus(uuid, clientKey, version));

        if (LimboIntegration.hasCompletedCaptcha(address, packetUsername)) {
            //Main.logError("passed name: " + passedCaptchaNameCache + " current name: " + name);
            /*if (!name.equals(passedCaptchaNameCache)) {
                this.disconnectWith(user, cachedNameDoesNotMatch);
                return;
            }*/
            //Since AlixChannelHandler.onLoginStart will not be invoked, add the connecting user manually
            if (!AlixChannelHandler.putConnecting(user, bedrockName)) return;

        } else if (!AlixChannelHandler.onLoginStart(user, packetUsername, bedrockName, data, performPremiumCheck))
            return;
        //Alix - end

        if (Dependencies.isBedrock(channel)) {
            //Floodgate player, do not handle, only retransmit the packet. The UUID will be set by Floodgate
            receiveFakeStartPacket(packetUsername, clientKey, channel, uuid);
            return;
        }
        //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/listener/AuthenticListeners.java#L29

        if (performPremiumCheck) {
            try {
                //should never happen
                if (newPremiumData.premiumUUID() == null) throw new AlixError("Null uuid during encryption send");

                byte[] token = EncryptionUtil.generateVerifyToken();

                WrapperLoginServerEncryptionRequest newPacket = new WrapperLoginServerEncryptionRequest("", keyPair.getPublic(), token);
                this.encryptionDataCache.put(user, new EncryptionData(packetUsername, bedrockName, token, clientKey, newPremiumData.premiumUUID()));

                ByteBuf dynamicWrapper = NettyUtils.dynamic(newPacket);
                ChannelHandlerContext silentCtx = NettyUtils.getSilentContext(channel);
                channel.eventLoop().execute(() -> {
                    silentCtx.write(dynamicWrapper);
                    //workaround to flush our current packet when FlushConsolidationHandler disallows to do so
                    channel.unsafe().flush();
                });
                /*channel.eventLoop().execute(() -> {
                    Main.debug("SENT ENCRYPTION REQ: " + name + " UUID: " + newPremiumData.premiumUUID() + " pipeline: " + channel.pipeline().names());
                    PacketEvents.getAPI().getProtocolManager().sendPacketSilently(user.getChannel(), newPacket);
                });*/

                /*channel.pipeline().addAfter("timeout", "sex", new ChannelDuplexHandler() {

                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        Main.logError("SENDING HANDLED");
                        super.write(ctx, msg, promise);
                    }

                    *//*@Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        Main.logError("SENDING HANDLED");
                        super.channelRead(ctx, msg);
                    }*//*
                });*/

                //Main.debug("SENDING ENCRYPTION REQ: " + name + " UUID: " + newPremiumData.premiumUUID() + " pipeline: " + channel.pipeline().names());

                /*NettyUtils.writeAndFlushDynamicWrapper(newPacket, NettyUtils.getSilentContext(channel)).addListener(f -> {
                   Main.debug("WRITE OP SUCCESS: " + f.isSuccess());
                });*/
                //PacketEvents.getAPI().getProtocolManager().sendPacket(user.getChannel(), newPacket);
                //Main.debug("SENDING DONE.");
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

    private void asyncPacketReceive(User user, PacketWrapper<?> wrapper, PacketType.Login.Client type) {

        //if (AlixUtils.isDebugEnabled) Main.logDebug("Packet received " + wrapper.getPacketType() + " from " + user.getName() + " (" + user.getAddress().toString() + ")");

        switch (type) {
            case LOGIN_START: {
                this.onLoginStart(user, (WrapperLoginClientLoginStart) wrapper);
                return;
            }
            case ENCRYPTION_RESPONSE: {
                this.onEncryptionResponse(user, (WrapperLoginClientEncryptionResponse) wrapper);
            }
        }
    }


    private void async(Runnable r) {
        AlixScheduler.asyncBlocking(r);
    }

    public void onPacketReceive(PacketLoginReceiveEvent event) {
        event.setCancelled(true);
        var type = event.getPacketType();
        var wrapper = switch (type) {
            case LOGIN_START -> new WrapperLoginClientLoginStart(event);
            case ENCRYPTION_RESPONSE -> new WrapperLoginClientEncryptionResponse(event);
            default -> throw new AlixError();
        };

        this.async(() -> {
            asyncPacketReceive(event.getUser(), wrapper, type);
            /* finally {
                copy.cleanUp();
            }*/
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
        if (premiumUUID == null) throw new AlixException("premiumUUID is null! Report this immediately!");
        AuthReflection.setUUID(networkManager, premiumUUID);
    }

    private void disconnectWith(User user, ByteBuf constDisconnectBuf) {
        NettyUtils.closeAfterConstSend((Channel) user.getChannel(), constDisconnectBuf);
    }

    private boolean verifyNonce(WrapperLoginClientEncryptionResponse packet,
                                ClientPublicKey clientPublicKey, byte[] expectedToken) {
        return PremiumVerifier.verifyNonce(packet, clientPublicKey, expectedToken, this.getServerVersion());
    }
}