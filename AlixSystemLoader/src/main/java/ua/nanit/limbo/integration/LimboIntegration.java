package ua.nanit.limbo.integration;

import alix.common.connection.filters.AntiVPN;
import alix.common.connection.filters.ConnectionManager;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumStatus;
import alix.common.data.premium.name.PremiumNameManager;
import alix.common.login.premium.ClientPublicKey;
import alix.common.login.premium.PremiumSetting;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.Messages;
import alix.common.utils.AlixCache;
import alix.common.utils.AlixCommonHandler;
import alix.common.utils.floodgate.GeyserUtil;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.google.common.cache.Cache;
import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.server.LimboServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static ua.nanit.limbo.integration.LimboIntegration.Packets.*;

public abstract class LimboIntegration<T extends ClientConnection> {

    protected static GeyserUtil GEYSER_UTIL;
    protected static final Map<String, InetAddress> completedCaptchaCache;
    //private static final Map<InetAddress, String> ;

    static {
        Cache<String, InetAddress> cache = AlixCache.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(300).build();
        completedCaptchaCache = cache.asMap();
    }
    //Netty integration
    //void invokeSilentServerChannelRead(Channel channel);

    /*default T newConnection(Channel channel, LimboServer server) {
        return (T) new ClientConnection(channel, server);
    }*/


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

        var ip = ((InetSocketAddress) channel.remoteAddress()).getAddress();
        return ip.equals(completedCaptchaCache.get(name));
    }

    public abstract T newConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state);


    //Packets
    public abstract void onHandshake(T connection, PacketHandshake handshake);

    public PreLoginResult onLoginStart(T connection, PacketLoginStart packet, boolean[] recode) {
        String nameSent = packet.getUsername();
        Channel channel = connection.getChannel();
        PersistentUserData data = UserFileManager.get(nameSent);
        UUID uuid = packet.getUUID();


        String nameRefactored = nameSent;
        Boolean hasCompletedCaptcha = null;


        //Premium handling

        ClientVersion version = connection.getClientVersion().getRetrooperVersion().toClientVersion();
        ClientPublicKey publicKey = ClientPublicKey.createKey(packet.getSignatureData());
        PremiumStatus suggestsStatus = PremiumUtils.suggestsStatus(uuid, publicKey, version);

            /*PremiumData premiumData = PremiumUtils.getOrRequestAndCacheData(data, nameSent);
            //the player can be premium if: either the nickname is premium or we couldn't determine if the nickname is premium
            boolean canBePremium = premiumData.getStatus().isPremium() || premiumData.getStatus().isUnknown();*/

        boolean isPremium = PremiumSetting.requirePremium(PersistentUserData.isRegistered(data), suggestsStatus) && PremiumUtils.getOrRequestAndCacheData(data, nameSent).getStatus().isPremium();

        //Main.debug("isPremium: " + isPremium + " suggestsStatus: " + suggestsStatus);
        //PersistentUserData prefixedNameData = UserFileManager.get(nameRefactored);

        boolean shouldReEncodeName = false;

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
            recode[0] = true;
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
        }

        //Intention hide
        boolean isTransfer = connection.getHandshakePacket().isTransfer();
        if (isTransfer && (hasCompletedCaptcha = hasCompletedCaptcha(channel, nameRefactored))) {
            connection.getHandshakePacket().setLoginIntention();
            recode[0] = true;
        }

        //AlixChannelHandler.getPreLoginVerdict(channel, nameSent, nameRefactored, data, isPremium)
        switch (AlixCommonHandler.getPreLoginVerdict(channel, nameSent, nameRefactored, data, isPremium, this.geyserUtil().isBedrock(channel))) {
            case ALLOWED:
                break;
            case DISALLOWED_INVALID_NAME:
                return disconnect(connection, invalidNamePacket);
            case DISALLOWED_MAX_ACCOUNTS_REACHED:
                return disconnect(connection, maxTotalAccountsPacket);
            case DISALLOWED_PREVENT_FIRST_JOIN:
                return disconnect(connection, preventFirstTimeJoinPacket);
            case DISALLOWED_VPN_DETECTED:
                return disconnect(connection, vpnDetectedPacket);
            default:
                throw new AlixError();
        }

        //TODO: DEBUG
        recode[0] = true;

        //Main.logInfo("REMAPPED: " + nameSent + " pr: " + nameRefactored);

        //todo: see if isPremium should stay here:
        //during premium -> non_premium switch the player hasn't passed the captcha,
        // but was let into the server without completing it - he must be shown the visual captcha now
        //|| isPremium
        return data != null || (hasCompletedCaptcha == Boolean.TRUE || hasCompletedCaptcha(channel, nameSent)) ? PreLoginResult.CONNECT_TO_MAIN_SERVER : PreLoginResult.CONNECT_TO_LIMBO;
    }

    private static PreLoginResult disconnect(ClientConnection connection, PacketSnapshot disconnectPacket) {
        connection.sendPacketAndClose(disconnectPacket);
        return PreLoginResult.DISCONNECTED;
    }

    static final class Packets {
        static final PacketSnapshot
                //alreadyConnectingPacket = PacketPlayOutDisconnect.snapshot(Messages.get("already-connecting")),
                invalidNamePacket = PacketLoginDisconnect.snapshot(Messages.get("anti-bot-invalid-name-blocked")),
                preventFirstTimeJoinPacket = PacketLoginDisconnect.snapshot(ConnectionManager.preventFirstTimeJoinMessage),
                maxTotalAccountsPacket = PacketLoginDisconnect.snapshot(GeoIPTracker.maxAccountsReached),
                vpnDetectedPacket = PacketLoginDisconnect.snapshot(AntiVPN.antiVpnMessage);
    }

    //Utils
    public abstract GeyserUtil geyserUtil();

    //Config
    public int getCompressionThreshold() {
        return 128;
    }

    public boolean isTransferAccepted() {
        return false;
    }

    //floodgate with compression disabled is present
    public boolean isFloodgateNoCompressionPresent() {
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