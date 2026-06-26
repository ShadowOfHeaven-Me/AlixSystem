package ua.nanit.limbo.integration;

import alix.common.antibot.algorithms.any.ConnectRequestAlgoImpl;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.filters.AntiVPN;
import alix.common.connection.filters.ConnectionManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumStatus;
import alix.common.data.premium.name.PremiumNameManager;
import alix.common.login.premium.ClientPublicKey;
import alix.common.login.premium.PremiumSetting;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.Messages;
import alix.common.utils.AlixCache;
import alix.common.utils.AlixCommonHandler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.floodgate.GeyserUtil;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static ua.nanit.limbo.integration.LimboIntegration.Packets.*;

public abstract class LimboIntegration<T extends ClientConnection> {

    protected static GeyserUtil GEYSER_UTIL;
    protected static final Map<String, InetAddress> completedCaptchaCache = AlixCache.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(300).<String, InetAddress>build().asMap();

    //Netty integration
    //void invokeSilentServerChannelRead(Channel channel);

    /*default T newConnection(Channel channel, LimboServer server) {
        return (T) new ClientConnection(channel, server);
    }*/

    public abstract boolean isProxyProtocol();


    public final void setHasCompletedCaptcha(InetAddress address, String name) {
        completedCaptchaCache.put(name, address);
    }

    /*public static boolean hasCompletedCaptcha(InetAddress address) {
        return completedCaptchaCache.get(address) != null;
    }

    public static boolean hasCompletedCaptcha(Channel channel) {
        return getCompletedCaptchaName(channel) != null;
    }*/

    //Captcha

    @Nullable
    public static boolean hasCompletedCaptcha(Channel channel, String name) {
        /*if (GEYSER_UTIL.isBedrock(channel))
            return true;*/

        var ip = AlixCommonUtils.getAddress(channel);
        return ip.equals(completedCaptchaCache.get(name));
    }


    public final ClientConnection newConnection(Channel channel, LimboServer server, boolean passActiveEvents) {
        return new ClientConnection(channel, server, passActiveEvents);
    }

    /*public void onCookieResponse(T connection, PacketInLoginCookieResponse response) {

    }*/

    //invoked whenever the real address become known
    public final void onProxyAddress(Channel channel, InetAddress address) {
        if (FireWallManager.isBlocked0(address)) {
            channel.close();
            return;
        }

        ConnectRequestAlgoImpl.onConnection(channel, address);
    }

    public void invokeChannelInit(ClientConnection connection) {
    }

    //Packets
    public abstract void onHandshake(T connection, PacketHandshake handshake);

    public void onLoginStart(T connection, PacketLoginStart packet, Consumer<PreLoginInfo> consumer) {
        String nameSent = packet.getUsername();
        PersistentUserData data = UserFileManager.get(nameSent);
        UUID uuid = packet.getUUID();

        //Premium handling

        ClientVersion version = connection.getClientVersion().getRetrooperVersion().toClientVersion();
        ClientPublicKey publicKey = ClientPublicKey.createKey(packet.getSignatureData());
        PremiumStatus suggestsStatus = PremiumUtils.suggestsStatus(uuid, publicKey, version);

        Consumer<PremiumData> premiumDataFuture = premiumData -> {
            boolean isPremium = premiumData.getStatus().isPremium();
            this.onLoginStart0(connection, nameSent, packet, data, isPremium, consumer);
        };

        if (data != null)
            //established status
            premiumDataFuture.accept(data.getPremiumData());
        else {
            //guessed status
            if (PremiumSetting.requirePremium(PersistentUserData.isRegistered(data), suggestsStatus))
                PremiumUtils.getOrRequestAndCacheData(connection.getChannel(), data, nameSent, premiumDataFuture);
            else premiumDataFuture.accept(PremiumData.UNKNOWN);
        }
    }

    private void onLoginStart0(T connection, String nameSent, PacketLoginStart packet, PersistentUserData data, boolean isPremium, Consumer<PreLoginInfo> consumer) {
        boolean shouldReEncodeName = false;
        boolean recode;

        Boolean hasCompletedCaptcha = null;
        String nameRefactored = nameSent;
        Channel channel = connection.getChannel();

        //data of the account <nameSent>
        if (data != null) {
            //both: the joining player and the existing data of the account <nameSent> have been determined to have the same isPremium
            // - just assume we can connect the player normally
            if (data.getPremiumData().getStatus().isPremium() == isPremium) {
                //nothing to do here
            } else {
                //their isPremium was determined to be different - recode the packet to the prefixed nameSent,
                // as that should be the correct player (can't tell of pre 1.19)
                shouldReEncodeName = true;
            }
        } else {//when data of account <nameSent> was null
            //get the data of the <nameSent> account, since the data of the account <nameSent>
            // could have been erased after we made the <prefixed nameSent> account
            PersistentUserData prefixedNameData = UserFileManager.get(nameRefactored);

            //we were right - the <prefixed nameSent> account of predicted premium status exists, so again
            //this *should* be our player (gotta guess tho)
            if (prefixedNameData != null) {
                shouldReEncodeName = true;
            }//else if the data of the accounts <nameSent> & <prefixed nameSent> is erased,
            // the player could always be logged in as <nameSent> (since that nickname would
            // now be free, the player could get stuck with the <nameSent> account, even if
            // he was previously on the <prefixed nameSent> account
            //the exact same thing could happen to the original owner of the <nameSent> account
        }

        boolean enabled = false;
        if (enabled && shouldReEncodeName) {
            recode = true;
            packet.wrapper().setUsername(nameRefactored);

            //special case:
            // map a premium player's nickname that is 16 characters long,
            // when a cracked player has made an account with the premium player's nickname
            // (we cannot simply substring(1) from the nickname to get the original,
            // since the minecraft protocol only allows nicknames up to 16 characters)
            if (nameSent.length() >= 16)
                PremiumNameManager.mapOverflow(nameRefactored, nameSent);

            nameRefactored = PremiumUtils.getPrefixedName(nameSent, isPremium);
            //nameSent = nameRefactored;
        } else {
            recode = false;
        }

        //AlixChannelHandler.getPreLoginVerdict(channel, nameSent, nameRefactored, data, isPremium)
        AlixCommonHandler.getPreLoginVerdict(channel, nameSent, nameRefactored, data, isPremium, this.geyserUtil().isBedrock(channel), this.isProxyProtocol(), verdict -> {
            switch (verdict) {
                case ALLOWED -> {
                }
                case DISALLOWED_INVALID_NAME -> {
                    consumer.accept(disconnect(connection, invalidNamePacket));
                    return;
                }
                case DISALLOWED_MAX_ACCOUNTS_REACHED -> {
                    consumer.accept(disconnect(connection, maxTotalAccountsPacket));
                    return;
                }
                case DISALLOWED_PREVENT_FIRST_JOIN -> {
                    consumer.accept(disconnect(connection, preventFirstTimeJoinPacket));
                    return;
                }
                case DISALLOWED_VPN_DETECTED -> {
                    consumer.accept(disconnect(connection, vpnDetectedPacket));
                    return;
                }
                case DISALLOWED_DIFFERENTLY_CASED_NAME_EXISTS -> {
                    consumer.accept(disconnect(connection, accountExistsUnderDifferentCase));
                    return;
                }
                default -> throw new AlixError();
            }
            if (geyserUtil().isBedrock(channel)) {
                consumer.accept(PreLoginInfo.MAIN_SERVER_NO_RECODE);
                return;
            }

            //TODO: DEBUG
            //recode = true;

            //Main.logInfo("REMAPPED: " + nameSent + " pr: " + nameRefactored);

            //todo: see if isPremium should stay here:
            //during premium -> non_premium switch the player hasn't passed the captcha,
            // but was let into the server without completing it - he must be shown the visual captcha now
            //|| isPremium
            boolean readyForLogin = data != null || (hasCompletedCaptcha == Boolean.TRUE || hasCompletedCaptcha(channel, nameSent));

            if (readyForLogin && !isPremium && this.limboLogin(connection, data)) {
                consumer.accept(new PreLoginInfo(PreLoginResult.CONNECT_TO_LIMBO, recode, StateSupplier.LOGIN));
                return;
            }

            var result = readyForLogin ? PreLoginResult.CONNECT_TO_MAIN_SERVER : PreLoginResult.CONNECT_TO_LIMBO;

            consumer.accept(new PreLoginInfo(result, recode, StateSupplier.CAPTCHA));
        });
    }

    public boolean limboLogin(ClientConnection connection, PersistentUserData data) {
        return false;
    }

    public void onLimboDisconnect(ClientConnection connection) {
        connection.getFrameDecoder().releaseCollected();
    }

    protected static PreLoginInfo disconnect(ClientConnection connection, PacketSnapshot disconnectPacket) {
        connection.sendPacketAndClose(disconnectPacket);
        return PreLoginInfo.EMPTY_DISCONNECTED;
    }

    static final class Packets {
        static final PacketSnapshot
                invalidNamePacket = PacketLoginDisconnect.snapshot(Messages.get("anti-bot-invalid-name-blocked")),
                preventFirstTimeJoinPacket = PacketLoginDisconnect.snapshot(ConnectionManager.preventFirstTimeJoinMessage),
                maxTotalAccountsPacket = PacketLoginDisconnect.snapshot(GeoIPTracker.maxAccountsReached),
                vpnDetectedPacket = PacketLoginDisconnect.snapshot(AntiVPN.antiVpnMessage),
                accountExistsUnderDifferentCase = PacketLoginDisconnect.snapshot(Messages.get("account-name-exists-under-different-casing"));
    }

    //Utils
    public abstract GeyserUtil geyserUtil();

    //Config
    public int getCompressionThreshold() {
        return 128;
    }

    public boolean isTransferAcceptable() {
        return false;
    }

    //floodgate with compression disabled is present
    public boolean isFloodgateNoCompressionPresent() {
        return false;
    }

    public boolean supportsCustomPayloadEvents() {
        return false;
    }

    public void fireCustomPayloadEvent(T connection, String channel, byte[] data) {
    }

    //Commands
    /*CommandHandler<T> createCommandHandler();

    //Transfer

    String getServerIP();

    int getPort();*/
}